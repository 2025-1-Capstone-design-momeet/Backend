package capstone2.backend.codes.service;

import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.entity.Minute;
import capstone2.backend.codes.repository.ClubRepository;
import capstone2.backend.codes.repository.MinuteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinuteService {
    private final MinuteRepository minuteRepository;
    private final ClubRepository clubRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${file.temp-dir}")
    private String tempDir;
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${ai.api.url}")
    private String aiApiUrl;

    public ResponseEntity<String> createMinute(MultipartFile file, String clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 클럽이 존재하지 않습니다: " + clubId));

        String minuteId = UUID.randomUUID().toString().replaceAll("-", "");

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!".wav".equals(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. .wav 파일만 허용됩니다.");
        }

        String fileName = minuteId + extension;
        Path targetPath = Paths.get(tempDir).resolve(fileName);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        long durationSeconds = 0L;
        try {
            File savedFile = targetPath.toFile();
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(savedFile);
            long frameLength = fileFormat.getFrameLength();
            float frameRate = fileFormat.getFormat().getFrameRate();
            durationSeconds = (int) (frameLength / frameRate);
        } catch (UnsupportedAudioFileException | IOException | ArithmeticException e) {
            durationSeconds = 0L;
        }

        Minute minute = new Minute(
                minuteId,
                club,
                LocalDateTime.now().minusSeconds(durationSeconds),
                null,
                null
        );
        minuteRepository.save(minute);

        // ✅ 응답 그대로 타고타고
        return sendToAIServer(targetPath.toFile(), minuteId);
    }


    private ResponseEntity<String> sendToAIServer(File file, String minuteId) {
        RestTemplate restTemplate = new RestTemplate();
        String targetUrl = aiApiUrl + "/process_audio/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("minuteId", minuteId);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForEntity(targetUrl, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            // db에서 minuteId로 minute 삭제
            minuteRepository.deleteById(minuteId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 서버 통신 오류: " + e.getMessage());
        }
    }

    public void saveCSVToMinute(MultipartFile file, String minuteId) {
        Minute minute = minuteRepository.findById(minuteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 minuteId의 회의록이 존재하지 않습니다: " + minuteId));

        String filename = minuteId + ".csv";
        Path targetPath = Paths.get(uploadDir).resolve(filename);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 저장 실패", e);
        }

        // 암호화된 경로 저장
        String encodedPath = passwordEncoder.encode(filename);
        minute.setFilePath(encodedPath);

        // 📋 요약 열에서 내용 추출
        String summary = readCSVSummary(targetPath);
        minute.setSummaryContents(summary);

        minuteRepository.save(minute);
    }

    public String readCSVSummary(Path csvPath) {
        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord record : parser) {
                if (record.isMapped("회의 요약")) {
                    String summary = record.get("회의 요약").trim();
                    if (!summary.isEmpty()) {
                        return summary;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        }

        return ""; // 요약 정보 없으면 빈 문자열 반환
    }

}

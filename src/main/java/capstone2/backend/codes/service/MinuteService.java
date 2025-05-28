package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.MinuteDto;
import capstone2.backend.codes.dto.MinuteListDto;
import capstone2.backend.codes.dto.ScriptLine;
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
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

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

    @Value("${ai.api.host}")
    private String aiServerHost;

    public ResponseEntity<String> createMinute(MultipartFile file, String clubId, int numSpeakers) {
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
            e.printStackTrace();
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

        return sendToAIServer(targetPath.toFile(), minuteId, numSpeakers);
    }

    private ResponseEntity<String> sendToAIServer(File file, String minuteId, int numSpeakers) {
        RestTemplate restTemplate = new RestTemplate();
        String targetUrl = aiApiUrl + "/process_audio/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Host", aiServerHost); // 👈 필수 헤더 추가

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("minuteId", minuteId);
        body.add("num_speakers", numSpeakers);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForEntity(targetUrl, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace(); // 👈 에러 로그 출력
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
            e.printStackTrace();
            throw new RuntimeException("CSV 파일 저장 실패", e);
        }

        String encodedPath = passwordEncoder.encode(filename);
        minute.setFilePath(encodedPath);

        Map<String, String> summaryInfo = readCSVTitleAndSummary(targetPath);
        minute.setSummaryContents(summaryInfo.get("title"));

        minuteRepository.save(minute);
    }

    private Map<String, String> readCSVTitleAndSummary(Path csvPath) {
        Map<String, String> result = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("Speaker", "Transcription"))) {
            for (CSVRecord record : parser) {
                String speaker = record.get("Speaker");
                String text = record.get("Transcription").trim();

                if ("회의 제목".equals(speaker)) {
                    result.put("title", text);
                } else if ("회의 요약".equals(speaker)) {
                    result.put("summary", text);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("CSV 제목/요약 읽기 실패", e);
        }
        return result;
    }

    public MinuteDto getMinuteDetails(String minuteId) {
        Minute minute = minuteRepository.findById(minuteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 minuteId의 회의록이 존재하지 않습니다: " + minuteId));

        Path path = Paths.get(uploadDir).resolve(minuteId + ".csv");
        Map<String, String> summaryInfo = readCSVTitleAndSummary(path);

        List<ScriptLine> script = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("Speaker", "Transcription"))) {
            for (CSVRecord record : parser) {
                String speaker = record.get("Speaker");
                String text = record.get("Transcription");

                if (!"회의 제목".equals(speaker) && !"회의 요약".equals(speaker)) {
                    script.add(new ScriptLine(speaker, text));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("CSV 읽기 실패", e);
        }

        String title = summaryInfo.getOrDefault("title", "현재 서버에서 처리 중입니다.");
        String summary = summaryInfo.getOrDefault("summary", "현재 서버에서 처리 중입니다.");

        return new MinuteDto(
                minute.getMinuteId(),
                minute.getDate(),
                title,
                summary,
                minute.getFilePath(),
                script
        );
    }

    public List<MinuteListDto> getMinutesByUserId(String userId) {
        try {
            List<Minute> minutes = minuteRepository.findMinutesByUserId(userId);
            List<MinuteListDto> minuteDtos = new ArrayList<>();
            for (Minute minute : minutes) {
                String title = (minute.getSummaryContents() != null && !minute.getSummaryContents().isEmpty())
                        ? minute.getSummaryContents() : "현재 서버에서 처리 중입니다.";
                minuteDtos.add(new MinuteListDto(
                        minute.getMinuteId(),
                        title,
                        minute.getDate()
                ));
            }
            return minuteDtos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("회의록 목록 조회 실패", e);
        }
    }
}

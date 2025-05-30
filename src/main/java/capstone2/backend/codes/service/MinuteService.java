package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.MinuteDetailDto;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
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
    private final AIClientService aiClientService;  // ✅ 비동기 처리 서비스

    @Value("${file.temp-dir}")
    private String tempDir;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String createMinute(MultipartFile file, String clubId, int numSpeakers) {
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
            System.out.println("파일 저장 성공: " + targetPath.toString());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        long durationSeconds = 0L;
        try {
            File savedFile = targetPath.toFile();
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(savedFile);
            long frameLength = fileFormat.getFrameLength();
            float frameRate = fileFormat.getFormat().getFrameRate();
            durationSeconds = (long) (frameLength / frameRate);
        } catch (Exception ignored) {}

        Minute minute = new Minute(
                minuteId,
                club,
                LocalDateTime.now().minusSeconds(durationSeconds),
                null,
                null
        );
        minuteRepository.save(minute);

        // ✅ 진짜 비동기 호출
        aiClientService.sendToAIServerAsync(targetPath.toFile(), minuteId, numSpeakers);

        return minuteId;
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

        String filePath = filename;
        minute.setFilePath(filePath);

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

    public MinuteDetailDto getMinuteDetails(String minuteId) {
        Minute minute = minuteRepository.findById(minuteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 minuteId의 회의록이 존재하지 않습니다: " + minuteId));

        Path path = Paths.get(uploadDir).resolve(minuteId + ".csv");

        Map<String, String> summaryInfo = new HashMap<>();
        List<ScriptLine> script = new ArrayList<>();

        // 파일이 존재하지 않거나 읽기 실패해도 예외 대신 기본값 반환
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path);
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("Speaker", "Transcription"))) {

                for (CSVRecord record : parser) {
                    String speaker = record.get("Speaker").trim();
                    String text = record.get("Transcription").trim();

                    if ("회의 제목".equals(speaker)) {
                        summaryInfo.put("title", text);
                    } else if ("회의 요약".equals(speaker)) {
                        summaryInfo.put("summary", text);
                    } else if (!"Speaker".equals(speaker)) {
                        script.add(new ScriptLine(speaker, text));
                    }
                }

            } catch (IOException e) {
                // 로그만 출력하고 넘어감
                e.printStackTrace();
            }
        } else {
            System.out.println("CSV 파일이 아직 생성되지 않았습니다: " + path);
        }

        String title = summaryInfo.getOrDefault("title", "현재 서버에서 처리 중입니다.");
        String summary = summaryInfo.getOrDefault("summary", "현재 서버에서 처리 중입니다.");

        return new MinuteDetailDto(
                minute.getMinuteId(),
                minute.getDate(),
                title,
                summary,
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
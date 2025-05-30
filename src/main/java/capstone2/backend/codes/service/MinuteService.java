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

        Path convertedPath = Paths.get(tempDir).resolve(minuteId + ".wav");

        try {
            if (!".wav".equals(extension)) {
                // ✅ 원본 파일 먼저 저장
                Path tempInputPath = Paths.get(tempDir).resolve(minuteId + extension);
                Files.copy(file.getInputStream(), tempInputPath, StandardCopyOption.REPLACE_EXISTING);

                // ✅ FFmpeg 실행 (aac/mp3/m4a 등을 wav로 변환)
                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg", "-y",
                        "-i", tempInputPath.toString(),
                        "-ar", "16000",             // 샘플링 레이트(예: Whisper는 16kHz 사용)
                        "-ac", "1",                 // 채널 수 (모노)
                        "-acodec", "pcm_s16le",     // WAV 표준 코덱
                        convertedPath.toString()
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) throw new RuntimeException("FFmpeg 변환 실패 (exitCode=" + exitCode + ")");
                System.out.println("FFmpeg 변환 완료: " + convertedPath);
            } else {
                // 이미 .wav인 경우 그냥 저장
                Files.copy(file.getInputStream(), convertedPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("WAV 파일 저장 완료: " + convertedPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("파일 저장 또는 변환 실패", e);
        }

        // ✅ 오디오 길이 계산
        long durationSeconds = 0L;
        try {
            File savedFile = convertedPath.toFile();
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

        // ✅ 비동기 전송
        aiClientService.sendToAIServerAsync(convertedPath.toFile(), minuteId, numSpeakers);
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
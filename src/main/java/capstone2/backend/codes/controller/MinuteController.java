package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.MinuteIdDto;
import capstone2.backend.codes.dto.UserIdRequestDto;
import capstone2.backend.codes.service.MinuteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/minute")
public class MinuteController {

    @Value("${file.temp-dir}")
    private String tempDir;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${ai.api.url}")
    private String aiServerUrl;

    @Value("${ai.api.host}")
    private String aiServerHost;

    private final RestTemplate restTemplate;
    private final MinuteService minuteService;

    @PostMapping("/create")
    public ResponseEntity<Response<?>> createMinute(
            @RequestPart("file") MultipartFile file,
            @RequestParam("clubId") String clubId,
            @RequestParam("num_speakers") int numSpeakers
    ) {
        try {
            // ✅ 더 이상 AI 응답을 기다리지 않음
            String minuteId = minuteService.createMinute(file, clubId, numSpeakers);
            return ResponseEntity.ok(
                    new Response<>("true", "회의 생성 요청 완료", Map.of("minuteId", minuteId))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "오류 발생", null));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMinuteCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("minuteId") String minuteId
    ) {
        try {
            minuteService.saveCSVToMinute(file, minuteId);
            return ResponseEntity.ok("CSV 업로드 및 저장 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV 저장 실패: " + e.getMessage());
        }
    }

    @PostMapping("/detail")
    public ResponseEntity<Response<?>> getMinuteDetail(@RequestBody MinuteIdDto minuteIdDto) {
        try {
            return ResponseEntity.ok(
                    new Response<>("true", "회의록 상세 조회 성공",
                            minuteService.getMinuteDetails(minuteIdDto.getMinuteId()))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "회의록 상세 조회 실패", null));
        }
    }

    @PostMapping("/list")
    public ResponseEntity<Response<?>> getMinutesByUserId(@RequestBody UserIdRequestDto userIdRequestDto) {
        try {
            return ResponseEntity.ok(
                    new Response<>("true", "회의록 목록 조회 성공",
                            minuteService.getMinutesByUserId(userIdRequestDto.getUserId()))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "회의록 목록 조회 실패", null));
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<?> pingAiServer() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Host", aiServerHost);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiServerUrl + "/",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return ResponseEntity.ok("AI 서버 응답: " + response.getStatusCode() + " - " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("AI 서버 연결 실패: " + e.getMessage());
        }
    }
}

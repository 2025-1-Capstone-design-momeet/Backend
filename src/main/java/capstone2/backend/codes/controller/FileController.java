package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class FileController {
    private final String uploadDir = "src/main/resources/minutes"; // 또는 외부 경로 추천

    @PostMapping("/upload")
    public ResponseEntity<Response<?>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("minuteId") String minuteId
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new Response<>("false", "빈 파일입니다.", null)
                );
            }

            String fileName = minuteId + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(
                    new Response<>("true", "파일 업로드 성공", fileName)
            );

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Response<>("false", "파일 저장 중 오류 발생", null)
            );
        }
    }

    // TODO: 유념!!! 개인별 list 조회 아님. 전체 minutes list라 수정해야함
    @GetMapping("/list")
    public ResponseEntity<Response<?>> listFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get(uploadDir), 1)) {
            List<String> fileNames = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new Response<>("success", "파일 목록 조회 성공", fileNames)
            );

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Response<>("fail", "파일 목록 조회 실패", null)
            );
        }
    }
}

package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.image-dir}")
    private String imageDir;
    @Value("${file.post-dir}")
    private String postDir;


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
                    new Response<>("true", "파일 목록 조회 성공", fileNames)
            );

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Response<>("false", "파일 목록 조회 실패", null)
            );
        }
    }


    @GetMapping("/image")
    public ResponseEntity<?> getImage(
            @RequestParam("type") String type,
            @RequestParam("filename") String filename
    ) {
        try {
            // 타입에 따라 기본 디렉토리 설정
            String baseDir;
            switch (type) {
                case "poster" -> baseDir = postDir; // @Value("${file.image-dir}")
                case "posts" -> baseDir = postDir; // @Value("${file.post-dir}")
                default -> {
                    return ResponseEntity.badRequest().body(
                            new Response<>("false", "지원하지 않는 타입입니다: " + type, null)
                    );
                }
            }

            Path imagePath = Paths.get(baseDir, filename).normalize();
            if (!imagePath.startsWith(Paths.get(baseDir))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Response<>("false", "허용되지 않은 경로입니다.", null));
            }

            if (!Files.exists(imagePath) || !Files.isReadable(imagePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("false", "이미지 파일이 존재하지 않거나 읽을 수 없습니다.", null));
            }

            // 파일의 MIME 타입을 추측
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 이미지 파일을 바이트 배열로 읽어오기
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "이미지 파일 반환 중 오류 발생", null));
        }
    }
}

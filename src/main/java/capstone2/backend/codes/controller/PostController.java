package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.ClubDto;
import capstone2.backend.codes.dto.PostDto;
import capstone2.backend.codes.dto.PostWriteDTO;
import capstone2.backend.codes.dto.UserDto;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    @PostMapping("/write")
    public ResponseEntity<Response<?>> writePost(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "postWriteDTO") PostWriteDTO postWriteDTO) {

        try {
            PostType type = PostType.fromCode(postWriteDTO.getType());

            if (!postService.writePost(postWriteDTO,file)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "게시글 작성에 실패했습니다.", null));
            }
            else {
                return ResponseEntity.ok(
                        new Response<>("true", String.format("%s 게시글 작성 성공", type.getLabel()), null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "게시글 작성에 실패했습니다.", null));
        }
    }

}

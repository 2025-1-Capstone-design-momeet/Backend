package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.ClubDto;
import capstone2.backend.codes.dto.PostDto;
import capstone2.backend.codes.dto.PostWriteDTO;
import capstone2.backend.codes.dto.UserDto;
import capstone2.backend.codes.entity.ClubPost;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.service.ClubService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;
    private final ClubService clubService;

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

    @PostMapping("/delete")
    public ResponseEntity<Response<?>> deletePost(
            @RequestPart(value = "postnum") String postnum) {

        try {
            if (!postService.deletePost(postnum)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "게시글 삭제에 실패했습니다.", null));
            }
            else {
                return ResponseEntity.ok(
                        new Response<>("true", "게시글 삭제 성공", null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "게시글 삭제에 실패했습니다.", null));
        }
    }

    @PostMapping("/get")
    public ResponseEntity<Response<?>> getPost(
            @RequestPart(value = "postnum") String postnum) {
        try {
            PostDto postDto = postService.getPost(postnum);
            return ResponseEntity.ok(
                    new Response<>("true", "게시글 가져오기 성공", postDto)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "게시글 가져오기에 실패했습니다.", null));
        }
    }

    @PostMapping("/getClubPostList")
    public ResponseEntity<Response<?>> getClubPostList(
            @RequestPart(value = "clubId") String clubId) {
        try {
            List<PostDto> postDtoList = postService.getClubPostList(clubId);
            return ResponseEntity.ok(
                    new Response<>("true", "동아리 전체 게시글 가져오기 성공", postDtoList)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "동아리 전체 게시글 가져오기에 실패했습니다.", null));
        }
    }

}

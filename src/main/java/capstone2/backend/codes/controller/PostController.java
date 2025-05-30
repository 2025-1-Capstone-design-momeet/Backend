package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.service.ClubService;
import capstone2.backend.codes.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    @PostMapping("/write")
    public ResponseEntity<Response<?>> writePost(
            @RequestPart("postWriteDTO") PostWriteDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            PostNumDto postNumDto = postService.savePost(dto, file);
            if (PostType.fromCode(dto.getType()) == PostType.GENERAL) {
                postService.createClubPost(postNumDto.getPostNum(), dto.getClubId());
            }
            else if (PostType.fromCode(dto.getType()) == PostType.POSTER) {
                postService.createPoster(postNumDto.getPostNum());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("false", "잘못된 게시글 유형입니다.", null));
            }
            return ResponseEntity.ok(new Response<>("true", "게시글 작성 성공", postNumDto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "게시글 작성 실패", null));
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Response<?>> deletePost(@RequestBody PostDeleteDto postDeleteDto) {
        try {
            if (!postService.deletePost(postDeleteDto)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "게시글 삭제에 실패했습니다.", null));
            } else {
                return ResponseEntity.ok(new Response<>("true", "게시글 삭제 성공", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "게시글 삭제에 실패했습니다.", null));
        }
    }

    @PostMapping("/get")
    public ResponseEntity<Response<?>> getPost(@RequestBody PostNumRequestDto postNumDto) {
        try {
            PostDto postDto = postService.getPost(postNumDto.getPostNum());
            return ResponseEntity.ok(new Response<>("true", "게시글 가져오기 성공", postDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "게시글 가져오기에 실패했습니다.", null));
        }
    }

    @PostMapping("/getClubPostList")
    public ResponseEntity<Response<?>> getClubPostList(@RequestBody ClubIdRequestDto clubIdDto) {
        try {
            List<PostDto> postDtoList = postService.getClubPostList(clubIdDto.getClubId());
            return ResponseEntity.ok(new Response<>("true", "동아리 전체 게시글 가져오기 성공", postDtoList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "동아리 전체 게시글 가져오기에 실패했습니다.", null));
        }
    }

}

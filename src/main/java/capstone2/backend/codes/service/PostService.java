package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.dto.PostWriteDto;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Service
@RequiredArgsConstructor
public class PostService {
    @Value("${file.post-dir}")
    private String postDir;

    private final PostRepository postRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubPostRepository clubPostRepository;
    private final PosterRepository posterRepository;

    // 게시글 찾기
    public boolean findPost(String postNum) throws Exception {
        try {
            return postRepository.existsById(postNum);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 게시글 삭제
    public boolean deletePost(PostDeleteDto postDeleteDto) throws Exception {

        try {
            Post post = postRepository.findById(postDeleteDto.getPostNum())
                    .orElseThrow(() -> new IllegalArgumentException("해당 postNum의 게시글을 찾을 수 없습니다."));
            User user = userRepository.findById(postDeleteDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 게시글을 찾을 수 없습니다."));

            if (Objects.equals(post.getUser().getUserId(), user.getUserId())) {
                postRepository.deleteById(post.getPostNum());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 게시글 가져오기
    public PostDto getPost(String postNum) throws Exception {
        try {
            Post post = postRepository.findById(postNum)
                    .orElseThrow(() -> new IllegalArgumentException("해당 postNum의 게시글을 찾을 수 없습니다."));

            return new PostDto(
                    post.getPostNum(),
                    post.getTitle(),
                    post.getContent(),
                    post.getType(),
                    PostType.fromCode(post.getType()).getLabel(),
                    post.getFile(),
                    post.getLike(),
                    post.getFixaction(),
                    post.getDate()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 동아리 게시글 리스트 가져오기
    @Transactional
    public List<PostDto> getClubPostList(String clubId) throws Exception {
        try {
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            List<ClubPost> clubPostList = club.getClubPosts();
            List<PostDto> postDtoList = new ArrayList<>();

            for (ClubPost clubPost : clubPostList) {
                postDtoList.add(getPost(clubPost.getPostNum()));
            }
            return postDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 게시글 파일 저장 성공
    public PostNumDto savePost(PostWriteDto dto, MultipartFile file) {
        try {
            String postNum = UUID.randomUUID().toString().replace("-", "");

            String filename = null;
            if (file != null && !file.isEmpty()) {
                filename = postNum + "_" + file.getOriginalFilename();
                Path targetPath = Paths.get(postDir).resolve(filename);
                try {
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("게시글 파일 저장 실패", e);
                }
            }

            User user = new User();
            user.setUserId(dto.getUserId());

            Post post = new Post(
                    postNum,
                    dto.getTitle(),
                    dto.getContent(),
                    dto.getType(),
                    filename,
                    0,
                    dto.getFixation(),
                    dto.getDate(),
                    null,
                    null,
                    user
            );

            postRepository.save(post);
            return new PostNumDto(post.getPostNum());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("게시글 저장 실패", e);
        }
    }

    // 동아리 게시물 추가s
    public void createClubPost(String postNum, String clubId) {
        try {
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            Post post = postRepository.findById(postNum)
                    .orElseThrow(() -> new IllegalArgumentException("해당 postNum의 게시글을 찾을 수 없습니다."));

            ClubPost clubPost = new ClubPost();
            clubPost.setPost(post);           // ✅ 필수
            clubPost.setClub(club);           // ✅ 연관관계

            clubPostRepository.save(clubPost);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("동아리 게시글 생성 실패", e);
        }
    }

    // 포스터 게시물 추가
    public void createPoster(String postNum) {
        try {
            Post post = postRepository.findById(postNum)
                    .orElseThrow(() -> new IllegalArgumentException("해당 postNum의 게시글을 찾을 수 없습니다."));
            Poster poster = new Poster();
            poster.setPost(post);
            poster.setImg(post.getFile());
            posterRepository.save(poster);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("포스터 생성 실패", e);
        }
    }
}


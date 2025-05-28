package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.ClubDto;
import capstone2.backend.codes.dto.PostDto;
import capstone2.backend.codes.dto.PostWriteDTO;
import capstone2.backend.codes.dto.UserDto;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.repository.ClubRepository;
import capstone2.backend.codes.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
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

    // 게시글 작성
    public boolean writePost(PostWriteDTO postWriteDTO, MultipartFile file) throws Exception {
        try {
            PostType type = PostType.fromCode(postWriteDTO.getType());
            String postNum = UUID.randomUUID().toString().replace("-", "");
            User user = new User(
                    postWriteDTO.getUserId(),
                    "password", "phoneNum", "name", "email",
                    null, true, null,
                    null, null, true
            );

            String filename = null;
            if(!file.isEmpty()){
                filename = postNum + "_" + file.getOriginalFilename() ;
                Path targetPath = Paths.get(postDir).resolve(filename);
                try {
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("게시글 파일 저장 실패", e);
                }
            }

            Post post = new Post(
                    postNum,
                    postWriteDTO.getTitle(),
                    postWriteDTO.getContent(),
                    postWriteDTO.getType(),
                    filename,
                    0,
                    postWriteDTO.getFixation(),
                    postWriteDTO.getDate(),
                    null,  // ClubPost는 아래에서 설정
                    null,   // Poster도 아직 없음
                    user
            );

            switch (type){
                case GENERAL -> {
                    Club club = new Club(
                            postWriteDTO.getClubId(),
                            "clubName", null, null, "category",
                            null, null, null, null
                    );
                    // ClubPost 객체 생성 및 연결
                    ClubPost clubPost = new ClubPost();
                    clubPost.setPostNum(postNum); // ID 설정
                    clubPost.setPost(post); // 연관관계 설정
                    clubPost.setClub(club);

                    post.setClubPost(clubPost);
                }
                case POSTER -> {
                    Poster poster = new Poster();
                    poster.setPostNum(postNum); // ID 설정
                    poster.setImg(Objects.requireNonNull(filename).toString());
                    poster.setPost(post); // 연관관계 설정

                    post.setPoster(poster);
                }
            }

            postRepository.save(post);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 게시글 찾기
    public boolean findPost(String postNum) throws Exception {
        try {
            if (postRepository.existsById(postNum)) {
                return true;
            }
            else{
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 게시글 삭제
    public boolean deletePost(String postNum) throws Exception {
        try {
            if (postRepository.existsById(postNum)) {
                postRepository.deleteById(postNum);
                return true;
            }
            else{
                return false;
            }
        }
        catch (Exception e){
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
                    post.getLike(),
                    post.getFixaction(),
                    post.getDate()
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 동아리 전체 게시글 가져오기
    @Transactional
    public List<PostDto> getClubPostList(String clubId) throws Exception {
        try{
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            List<ClubPost> clubPostList = club.getClubPosts(); // 연결된 모든 게시글
            List<PostDto> postDtoList = new ArrayList<>();

            for (ClubPost clubPost : clubPostList) {
                postDtoList.add(getPost(clubPost.getPostNum()));
            }
            return postDtoList;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }


}

package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.PostWriteDTO;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PostService {
    @Value("${file.post-dir}")
    private String postDir;
    private final PostRepository postRepository;

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
}

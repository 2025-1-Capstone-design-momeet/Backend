package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.ClubPost;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ClubPostRepository extends JpaRepository<ClubPost, String> {
    @EntityGraph(attributePaths = {"post", "club", "club.clubPromotion", "post.poster"})
    Optional<ClubPost> findByPostNum(String postNum);
    List<ClubPost> findTop3ByClub_ClubIdOrderByPost_FixactionDescPost_DateDesc(String clubId);
}

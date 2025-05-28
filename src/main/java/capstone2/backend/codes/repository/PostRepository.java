package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import capstone2.backend.codes.dto.AllPostDto;
import capstone2.backend.codes.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {

    @Query("""
    SELECT new capstone2.backend.codes.dto.AllPostDto(
        p.postNum,
        p.title,
        p.content,
        p.type,
        c.clubName,
        c.univName,
        poster.img,
        p.date
    )
    FROM Post p
    LEFT JOIN p.clubPost cp
    LEFT JOIN cp.club c
    LEFT JOIN p.poster poster
    WHERE (:univName IS NULL OR c.univName = :univName)
    ORDER BY p.date DESC
""")
    List<AllPostDto> findAllPostsByUnivName(@Param("univName") String univName);


}

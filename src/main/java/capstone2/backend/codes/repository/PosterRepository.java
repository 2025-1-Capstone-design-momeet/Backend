package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Poster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PosterRepository extends JpaRepository<Poster, String> {
    @Query("""
    SELECT p
    FROM Poster p
    JOIN p.post po
    JOIN po.clubPost cp
    JOIN cp.club c
    WHERE c.univName = :univName
""")
    List<Poster> findAllByUnivName(@Param("univName") String univName);
}

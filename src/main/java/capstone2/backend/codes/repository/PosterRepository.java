package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Poster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PosterRepository extends JpaRepository<Poster, String> {
    @Query("""
    SELECT DISTINCT p
    FROM Poster p
    JOIN FETCH p.post po
    JOIN FETCH po.clubPost cp
    JOIN FETCH cp.club c
    LEFT JOIN FETCH cp.club.clubPromotion
    WHERE c.univName = :univName
    """)
    List<Poster> findAllByUnivName(@Param("univName") String univName);
        @Query("""
            SELECT DISTINCT p
            FROM Poster p
            JOIN FETCH p.post po
            JOIN FETCH po.clubPost cp
            JOIN FETCH cp.club c
            LEFT JOIN FETCH cp.club.clubPromotion
            WHERE c.univName = :univName
            AND po.date >= :cutoff
        """)
        List<Poster> findAllByUnivNameWithinAWeek(
                @Param("univName") String univName,
                @Param("cutoff") LocalDateTime cutoff
        );

}

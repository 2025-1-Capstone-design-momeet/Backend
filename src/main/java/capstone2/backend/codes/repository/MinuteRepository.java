package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Minute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MinuteRepository extends JpaRepository<Minute, String> {
    @Query("""
        SELECT m
        FROM Minute m
        JOIN FETCH m.club cs
        JOIN ClubMembers cm ON cs.clubId = cm.clubId
        WHERE cm.userId = :userId
    """)
    List<Minute> findMinutesByUserId(String clubId);

    // 방법 1: 쿼리 메서드 (자동 생성)
    List<Minute> findByClub_ClubId(String clubId);

}

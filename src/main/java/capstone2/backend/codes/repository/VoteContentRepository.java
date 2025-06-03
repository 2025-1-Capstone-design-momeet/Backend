package capstone2.backend.codes.repository;

import capstone2.backend.codes.dto.VoteContentDto;
import capstone2.backend.codes.entity.User;
import capstone2.backend.codes.entity.VoteContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteContentRepository extends JpaRepository<VoteContent, String> {
    // 해당 번호를 선택한 회원수 가져오기
    @Query(
            value = "SELECT vc.voteContentId AS voteContentID, vc.voteID AS voteID, vc.field AS field, vc.voteNum AS voteNum, COUNT(vs.userId) AS voteContentNum " +
                    "FROM VoteContent vc " +
                    "LEFT JOIN VoteState vs ON vc.voteContentId = vs.voteContentId " +
                    "WHERE vc.voteID = :voteId " +
                    "GROUP BY vc.voteContentId, vc.voteID, vc.field, vc.voteNum",
            nativeQuery = true)
    List<Object[]> findVoteContentWithCount(@Param("voteId") String voteId);

    // 해당 번호를 선택한 동아리 회원 가져오기
    @Query(
            value = "SELECT u.* " +
                    "FROM VoteState vs " +
                    "JOIN User u ON vs.userId = u.userId " +
                    "WHERE vs.voteID = :voteId AND vs.voteContentId = :voteContentId",
            nativeQuery = true)
    List<User> findUsersByVoteContentAndContentId(
            @Param("voteId") String voteId,
            @Param("voteContentId") String voteContentId);

    // 해당 번호를 선택한 동아리 회원의 수(count)
    @Query(
            value = "SELECT COUNT(*) " +
                    "FROM VoteState vs " +
                    "JOIN User u ON vs.userId = u.userId " +
                    "WHERE vs.voteID = :voteId AND vs.voteContentId = :voteContentId",
            nativeQuery = true)
    int countUsersByVoteContentAndContentId(
            @Param("voteId") String voteId,
            @Param("voteContentId") String voteContentId);


    // 해당 번호를 선택하지 않은 동아리 회원 전체 가져오기
    @Query(
            value = "SELECT u.* " +
                    "FROM ClubMembers cm " +
                    "JOIN User u ON cm.userId = u.userId " +
                    "WHERE cm.clubId = :clubId " +
                    "AND u.userId NOT IN ( " +
                    "    SELECT vs.userId " +
                    "    FROM VoteState vs " +
                    "    WHERE vs.voteContentId = :voteContentId " +
                    ")",
            nativeQuery = true)
    List<User> findUsersNotSelectedVoteContent(
            @Param("clubId") String clubId,
            @Param("voteContentId") String voteContentId
    );

}

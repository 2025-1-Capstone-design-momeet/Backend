package capstone2.backend.codes.repository;


import capstone2.backend.codes.entity.Vote;
import capstone2.backend.codes.entity.VoteState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface VoteStateRepository extends JpaRepository<VoteState, String> {
    VoteState findByUserIdAndVoteID(String userId, String voteID);
    Optional<VoteState> findByUser_UserIdAndVote_VoteID(String userId, String voteID);

}

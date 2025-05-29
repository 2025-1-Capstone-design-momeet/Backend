package capstone2.backend.codes.repository;


import capstone2.backend.codes.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VoteRepository extends JpaRepository<Vote, String> {

}

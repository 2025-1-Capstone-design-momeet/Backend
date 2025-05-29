package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.VoteContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteContentRepository extends JpaRepository<VoteContent, String> {
}

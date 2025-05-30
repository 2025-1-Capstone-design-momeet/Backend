package capstone2.backend.codes.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteId implements Serializable {
    private String userId;
    private String voteID;
    private String voteContentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoteId that)) return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(voteID, that.voteID) &&
                Objects.equals(voteContentId, that.voteContentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, voteID, voteContentId);
    }
}

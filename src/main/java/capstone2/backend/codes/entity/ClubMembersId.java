package capstone2.backend.codes.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembersId implements Serializable {
    private String userId;
    private String clubId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClubMembersId that)) return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(clubId, that.clubId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, clubId);
    }
}

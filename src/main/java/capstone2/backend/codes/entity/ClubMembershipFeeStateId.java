package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembershipFeeStateId implements Serializable {
    private String membershipFeeId;
    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClubMembershipFeeStateId that)) return false;
        return Objects.equals(membershipFeeId, that.membershipFeeId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(membershipFeeId, userId);
    }
}
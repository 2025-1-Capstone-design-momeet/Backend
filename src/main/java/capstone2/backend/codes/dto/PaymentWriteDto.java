package capstone2.backend.codes.dto;

import capstone2.backend.codes.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWriteDto {
    private String userId;
    private String voteID;
    private String clubId;
    private String title;
    private int amount;
    private String account;

    private List<String> paymentMembers;
}
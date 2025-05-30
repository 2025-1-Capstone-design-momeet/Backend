package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymentHistoryDto {
    private String payId;
    private String clubId;
    private String title;
    private int amount;
    private String account;
}

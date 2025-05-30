package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymentStateDto {
    private String payId;
    private String userId;
    private String name;
    private boolean hasPaid = false;
}

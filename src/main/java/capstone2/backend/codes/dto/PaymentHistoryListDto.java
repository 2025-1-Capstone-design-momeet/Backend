package capstone2.backend.codes.dto;

import capstone2.backend.codes.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymentHistoryListDto {
    private List<PaymentHistoryDto> completePay;
    private List<PaymentHistoryDto> uncompletePay;
}


package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutiveRequestDto {
    private String clubId;
    private String userId;
    private String newExecutiveId;
    private String duty; // 직책 (예: 부회장, 총무 등)
}

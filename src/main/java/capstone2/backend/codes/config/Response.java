package capstone2.backend.codes.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
public class Response<T> {
    private String success;
    private String message;
    private T data;
}

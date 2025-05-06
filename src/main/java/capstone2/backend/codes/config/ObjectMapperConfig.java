package capstone2.backend.codes.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        //DTO에서 LocalDateTime 쓰기 위해
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
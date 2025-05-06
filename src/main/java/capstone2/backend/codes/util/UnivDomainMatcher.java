package capstone2.backend.codes.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class UnivDomainMatcher {

    private final Map<String, String> domainToUniv = new HashMap<>();

    @PostConstruct
    public void loadJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/univ/univ-domains.json");
        JsonNode node = mapper.readTree(is);
        for (JsonNode univ : node) {
            domainToUniv.put(univ.get("domain").asText(), univ.get("name").asText());
        }
    }

    public String match(String email) {
        if (email == null || !email.contains("@")) return null;
        String domain = email.split("@")[1].toLowerCase();
        return domainToUniv.get(domain);
    }
}
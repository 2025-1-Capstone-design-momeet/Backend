package capstone2.backend.codes.service;

import capstone2.backend.codes.repository.MinuteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Service
public class AIClientService {

    private final MinuteRepository minuteRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.host}")
    private String aiServerHost;

    public AIClientService(MinuteRepository minuteRepository) {
        this.minuteRepository = minuteRepository;
    }

    @Async
    public void sendToAIServerAsync(File file, String minuteId, int numSpeakers) {
        System.out.println("AI 서버로 전송 시작: " + file.getAbsolutePath());
        String targetUrl = aiApiUrl + "/process_audio/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Host", aiServerHost);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("minuteId", minuteId);
        body.add("num_speakers", numSpeakers);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            System.out.println("AI 서버로 요청 전송: " + targetUrl);
            restTemplate.postForEntity(targetUrl, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            minuteRepository.deleteById(minuteId);  // 실패 시 rollback
        }
    }
}

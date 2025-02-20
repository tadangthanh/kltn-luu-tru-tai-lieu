package vn.kltn.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.stereotype.Service;
import vn.kltn.service.IGoogleService;

import java.io.IOException;

@Service
@Slf4j(topic = "GOOGLE_SERVICE")
@RequiredArgsConstructor
public class GoogleServiceImpl implements IGoogleService {
    private final Client client;

    @Override
    public String generateContent(String content) {
        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash-001", content, null);
            System.out.println("Unary response: " + response.text());
            return response.text();
        } catch (IOException | HttpException e) {
            log.error("Error while generating content: {}", e.getMessage());
            return "loi";
        }
    }
}

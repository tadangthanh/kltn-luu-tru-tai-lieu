package vn.kltn.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vn.kltn.service.IGoogleService;

import java.io.IOException;
import java.net.URI;

@Service
@Slf4j(topic = "GOOGLE_SERVICE")
@RequiredArgsConstructor
public class GoogleServiceImpl implements IGoogleService {
    private final Client client;
    @Value("${google.api-key}")
    private String googleGeminiApiKey;
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // Tăng giới hạn lên 10MB
            .build();

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

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

    @Override
    public String askGemini(String question, String url) {
        try {
            // 1. Tải PDF
            byte[] pdfBytes = webClient.get()
                    .uri(new URI(url))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            // 2. Chuyển sang Base64
            String base64PDF = Base64.encodeBase64String(pdfBytes);

            // 3. Gọi Gemini API
            String response = webClient.post()
                    .uri(GEMINI_URL + "?key=" + googleGeminiApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(buildRequestBody(base64PDF, question))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return extractAnswer(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Hàm trích xuất câu trả lời từ JSON response
    private String extractAnswer(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode textNode = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            return textNode.asText(); // Chỉ lấy nội dung câu trả lời

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi xử lý phản hồi từ AI";
        }
    }

    private String buildRequestBody(String base64PDF, String question) {
        return String.format("""
                {
                    "contents": [{
                        "parts": [
                            {"inlineData": {"mimeType": "application/pdf", "data": "%s"}},
                            {"text": "%s"}
                        ]
                    }]
                }""", base64PDF, question);
    }
}

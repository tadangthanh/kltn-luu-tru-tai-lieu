package vn.kltn.service;

public interface IGoogleService {
    String generateContent(String content);
    String askGemini(String question,String url);
}

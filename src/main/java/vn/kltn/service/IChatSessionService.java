package vn.kltn.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import vn.kltn.dto.ChatSessionDto;
import vn.kltn.dto.request.ChatSessionInit;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.ChatSession;

import java.util.List;

public interface IChatSessionService {
    ChatSessionDto createChat(ChatSessionDto chatSessionDto);

    ChatSessionDto getChatSessionByDocId(Long docId);

    void deleteChat(Long id);

    ChatSession getChatSessionById(Long id);

    PageResponse<List<ChatSessionDto>> getAllChatSession(Pageable pageable);

    ChatSessionDto createChat(ChatSessionInit chatSessionInit);

    ChatSessionDto updateChatSession(Long id, ChatSessionDto chatSessionDto);
}

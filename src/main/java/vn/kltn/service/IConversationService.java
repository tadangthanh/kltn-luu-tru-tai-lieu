package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.ConversationRequest;
import vn.kltn.dto.response.ConversationDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.ChatSession;
import vn.kltn.entity.Conversation;

import java.util.List;

public interface IConversationService {
    ConversationDto addConversation(ConversationRequest conversationRequest);

    PageResponse<List<ConversationDto>> getAllByChatSessionId(Long assistantFileId, Pageable pageable);

    Conversation save(ChatSession chatSession, ConversationDto conversationDto);

    List<ConversationDto> getAllByChatSessionId(Long assistantFileId);
}

package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.ConversationDto;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public interface IConversationService {
    ConversationDto addConversation(ConversationDto conversationDto);

    PageResponse<List<ConversationDto>> getAllByAssistantFileId(Long assistantFileId, Pageable pageable);

}

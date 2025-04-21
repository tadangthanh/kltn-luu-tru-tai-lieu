package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.ConversationRequest;
import vn.kltn.dto.response.ConversationDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.ChatSession;
import vn.kltn.entity.Conversation;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.ConversationMapper;
import vn.kltn.repository.ChatSessionRepo;
import vn.kltn.repository.ConversationRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IConversationService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CONVERSATION_SERVICE")
public class ConversationServiceImpl implements IConversationService {
    private final ConversationMapper conversationMapper;
    private final ConversationRepo conversationRepo;
    private final IAuthenticationService authenticationService;
//    private final IChatSessionService chatSessionService;
    private final ChatSessionRepo chatSessionRepo;
    @Override
    public ConversationDto addConversation(ConversationRequest conversationRequest) {
        log.info("Add conversation to chat session id: {}", conversationRequest.getChatSessionId());
        ChatSession chatSession = chatSessionRepo.findById(conversationRequest.getChatSessionId()).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        validateChatSessionBelongToUser(chatSession);
        Conversation conversation = conversationMapper.toEntity(conversationRequest);
        conversationRepo.save(conversation);
        conversation.setChatSession(chatSession);
        return conversationMapper.toResponse(conversation);
    }

    private void validateChatSessionBelongToUser(ChatSession chatSession) {
        if (!chatSession.getUser().getId().equals(authenticationService.getCurrentUser().getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào tệp trợ lý này");
        }
    }

    @Override
    public PageResponse<List<ConversationDto>> getAllByChatSessionId(Long chatSessionId, Pageable pageable) {
        log.info("Get all conversations by assistantFileId: {}", chatSessionId);
        ChatSession chatSession = chatSessionRepo.findById(chatSessionId).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        validateChatSessionBelongToUser(chatSession);
        Page<Conversation> conversations = conversationRepo.findAllByChatSessionId(chatSessionId, pageable);
        List<ConversationDto> conversationList = conversations.stream()
                .map(conversationMapper::toResponse)
                .toList();
        return PageResponse.<List<ConversationDto>>builder()
                .items(conversationList)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(conversations.getTotalPages())
                .totalItems(conversations.getTotalElements())
                .hasNext(conversations.hasNext())
                .build();
    }

    @Override
    public Conversation save(ChatSession chatSession, ConversationDto conversationDto) {
        if (conversationDto == null) {
            return null;
        }

        Conversation conversation = conversationMapper.toEntity(conversationDto);
        conversation.setChatSession(chatSession);
        return conversationRepo.save(conversation);
    }


    @Override
    public List<ConversationDto> getAllByChatSessionId(Long chatSessionId) {
        return conversationRepo.findAllByChatSessionId(chatSessionId).stream().map(conversationMapper::toResponse).toList();
    }
}

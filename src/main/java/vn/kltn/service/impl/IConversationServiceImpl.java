package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.dto.ConversationDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.ChatSession;
import vn.kltn.entity.Conversation;
import vn.kltn.map.ConversationMapper;
import vn.kltn.repository.ConversationRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IChatSessionService;
import vn.kltn.service.IConversationService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CONVERSATION_SERVICE")
public class IConversationServiceImpl implements IConversationService {
    private final ConversationMapper conversationMapper;
    private final ConversationRepo conversationRepo;
    private final IAuthenticationService authenticationService;
    private final IChatSessionService chatSessionService;

    @Override
    public ConversationDto addConversation(ConversationDto conversationDto) {
        log.info("Add conversation to chat session id: {}", conversationDto.getChatSessionId());
        Conversation conversation = conversationMapper.toEntity(conversationDto);
        conversationRepo.save(conversation);
        ChatSession chatSession = chatSessionService.getChatSessionById(conversationDto.getChatSessionId());
        validateChatSessionBelongToUser(chatSession);
        conversation.setChatSession(chatSession);
        return conversationMapper.toDto(conversation);
    }

    private void validateChatSessionBelongToUser(ChatSession chatSession) {
        if (!chatSession.getUser().getId().equals(authenticationService.getCurrentUser().getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào tệp trợ lý này");
        }
    }

    @Override
    public PageResponse<List<ConversationDto>> getAllByChatSessionId(Long chatSessionId, Pageable pageable) {
        log.info("Get all conversations by assistantFileId: {}", chatSessionId);
        ChatSession chatSession = chatSessionService.getChatSessionById(chatSessionId);
        validateChatSessionBelongToUser(chatSession);
        Page<Conversation> conversations = conversationRepo.findAllByChatSessionId(chatSessionId, pageable);
        List<ConversationDto> conversationList = conversations.stream()
                .map(conversationMapper::toDto)
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
    public List<ConversationDto> getAllByChatSessionId(Long chatSessionId) {
        return conversationRepo.findAllByChatSessionId(chatSessionId).stream().map(conversationMapper::toDto).toList();
    }
}

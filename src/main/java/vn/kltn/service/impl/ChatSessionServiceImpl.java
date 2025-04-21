package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.dto.ChatSessionDto;
import vn.kltn.dto.request.ChatSessionInit;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.AssistantFile;
import vn.kltn.entity.ChatSession;
import vn.kltn.entity.Conversation;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.ChatSessionMapper;
import vn.kltn.repository.ChatSessionRepo;
import vn.kltn.service.IAssistantFileService;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IChatSessionService;
import vn.kltn.service.IConversationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "CHAT_SESSION_SERVICE")
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements IChatSessionService {
    private final ChatSessionRepo chatSessionRepo;
    private final ChatSessionMapper chatSessionMapper;
    private final IAuthenticationService authenticationService;
    private final IAssistantFileService assistantFileService;
    private final IConversationService conversationService;

    @Override
    public ChatSessionDto createChat(ChatSessionDto chatSessionDto) {
        log.info("Create chat session: name {}", chatSessionDto.getName());
        ChatSession chatSession = chatSessionMapper.toEntity(chatSessionDto);
        chatSession.setUser(authenticationService.getCurrentUser());
        chatSession = chatSessionRepo.save(chatSession);
        return chatSessionMapper.toDto(chatSession);
    }

    @Override
    public void deleteChat(Long id) {
        ChatSession chatSessionExist = chatSessionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!chatSessionExist.getUser().getId().equals(authenticationService.getCurrentUser().getId())) {
            throw new AccessDeniedException("You are not the owner of this chat session");
        }
        log.info("Delete chat session: id {}", id);
        chatSessionRepo.delete(chatSessionExist);
    }

    @Override
    public ChatSession getChatSessionById(Long id) {
        return chatSessionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
    }

    @Override
    public PageResponse<List<ChatSessionDto>> getAllChatSession(Pageable pageable) {
        log.info("Get all chat session");
        Page<ChatSession> sessionPage = chatSessionRepo.findAllByUser(authenticationService.getCurrentUser().getId(), pageable);
        List<ChatSessionDto> chatSessionDtoList = sessionPage.getContent().stream()
                .map(chatSessionMapper::toDto)
                .toList();
        return PageResponse.<List<ChatSessionDto>>builder()
                .items(chatSessionDtoList)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(sessionPage.getTotalPages())
                .totalItems(sessionPage.getTotalElements())
                .hasNext(sessionPage.hasNext())
                .build();
    }

    @Override
    public ChatSessionDto createChat(ChatSessionInit chatSessionInit) {
        log.info("Create chat session: name {}", chatSessionInit.getConversation().getQuestion());

        // 1. Tạo và lưu ChatSession trước
        ChatSession chatSession = chatSessionMapper.requestToEntity(chatSessionInit);
        chatSession.setUser(authenticationService.getCurrentUser());
        chatSession.setAssistantFiles(new HashSet<>()); // Khởi tạo collection
        chatSession.setConversations(new ArrayList<>()); // Khởi tạo collection

        // 2. Lưu ChatSession để có ID
        chatSession = chatSessionRepo.save(chatSession);

        // 3. Lưu AssistantFiles và thêm vào collection của ChatSession
        List<AssistantFile> assistantFiles = assistantFileService.save(chatSession, chatSessionInit.getAssistantFiles());
        chatSession.getAssistantFiles().addAll(assistantFiles);

        // 4. Lưu Conversation và thêm vào collection của ChatSession
        Conversation conversation = conversationService.save(chatSession, chatSessionInit.getConversation());
        chatSession.getConversations().add(conversation);

        // 5. Cập nhật lại ChatSession với các collections đã được populate
        chatSession = chatSessionRepo.save(chatSession);

        return chatSessionMapper.toDto(chatSession);
    }

}

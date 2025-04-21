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
import vn.kltn.entity.AssistantFile;
import vn.kltn.entity.Conversation;
import vn.kltn.map.ConversationMapper;
import vn.kltn.repository.ConversationRepo;
import vn.kltn.service.IAssistantFileService;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IConversationService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CONVERSATION_SERVICE")
public class IConversationServiceImpl implements IConversationService {
    private final ConversationMapper conversationMapper;
    private final ConversationRepo conversationRepo;
    private final IAssistantFileService assistantFileService;
    private final IAuthenticationService authenticationService;

    @Override
    public ConversationDto addConversation(ConversationDto conversationDto) {
        log.info("Add conversation to assistantFile: {}", conversationDto.getAssistantFileId());
        Conversation conversation = conversationMapper.toEntity(conversationDto);
        conversationRepo.save(conversation);
        AssistantFile assistantFile = assistantFileService.getFileById(conversationDto.getAssistantFileId());
        validateAssistantFileBelongToCurrentUser(assistantFile);
        conversation.setAssistantFile(assistantFile);
        return conversationMapper.toDto(conversation);
    }

    private void validateAssistantFileBelongToCurrentUser(AssistantFile assistantFile) {
        if (!assistantFile.getUser().getId().equals(authenticationService.getCurrentUser().getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào tệp trợ lý này");
        }
    }

    @Override
    public PageResponse<List<ConversationDto>> getAllByAssistantFileId(Long assistantFileId, Pageable pageable) {
        log.info("Get all conversations by assistantFileId: {}", assistantFileId);
        AssistantFile assistantFile = assistantFileService.getFileById(assistantFileId);
        validateAssistantFileBelongToCurrentUser(assistantFile);
        Page<Conversation> conversations = conversationRepo.findAllByAssistantFile(assistantFileId, pageable);
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
    public List<ConversationDto> getAllByAssistantFileId(Long assistantFileId) {
        return conversationRepo.findAllByAssistantFileId(assistantFileId).stream().map(conversationMapper::toDto).toList();
    }
}

package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.ConversationRequest;
import vn.kltn.dto.response.ConversationDto;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IConversationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversation")
@Validated
public class ConversationRest {
    private final IConversationService conversationService;
    @PostMapping
    public ResponseData<ConversationDto> addConversation(@Valid @RequestBody ConversationRequest conversationRequest) {
        return new ResponseData<>(201, "Thành công", conversationService.addConversation(conversationRequest));
    }
    @GetMapping("/assistant-file/{assistantFileId}")
    public ResponseData<?> getAllByAssistantFileId(@PathVariable Long assistantFileId, Pageable pageable) {
        return new ResponseData<>(200, "Thành công", conversationService.getAllByChatSessionId(assistantFileId, pageable));
    }
    @GetMapping("/all/assistant-file/{assistantFileId}")
    public ResponseData<?> getAllByAssistantFileId(@PathVariable Long assistantFileId) {
        return new ResponseData<>(200, "Thành công", conversationService.getAllByChatSessionId(assistantFileId));
    }
}

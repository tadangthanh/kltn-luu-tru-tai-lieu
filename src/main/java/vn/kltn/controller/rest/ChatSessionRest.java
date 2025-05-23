package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.ChatSessionDto;
import vn.kltn.dto.request.ChatSessionInit;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IChatSessionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-session")
@Validated
public class ChatSessionRest {
    private final IChatSessionService chatSessionService;

    @PostMapping
    public ResponseData<ChatSessionDto> createChat(@Valid @RequestBody ChatSessionDto chatSessionDto) {
        return new ResponseData<>(201, "Thành công", chatSessionService.createChat(chatSessionDto));
    }

    @PostMapping("/init-chat")
    public ResponseData<ChatSessionDto> initChat(@Valid @RequestBody ChatSessionInit chatSessionInit) {
        return new ResponseData<>(201, "Thành công", chatSessionService.createChat(chatSessionInit));
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteChat(@PathVariable Long id) {
        chatSessionService.deleteChat(id);
        return new ResponseData<>(200, "Xóa thành công", null);
    }

    @GetMapping
    public ResponseData<PageResponse<List<ChatSessionDto>>> getAllChatSession(Pageable pageable) {
        return new ResponseData<>(200, "Thành công", chatSessionService.getAllChatSession(pageable));
    }
    @GetMapping("/doc/{docId}")
    public ResponseData<ChatSessionDto> getChatSessionByDocId(@PathVariable Long docId) {
        return new ResponseData<>(200, "Thành công", chatSessionService.getChatSessionByDocId(docId));
    }
    @PutMapping("/{id}")
    public ResponseData<ChatSessionDto> updateChatSession(@PathVariable Long id, @Valid @RequestBody ChatSessionDto chatSessionDto) {
        return new ResponseData<>(200, "Thành công", chatSessionService.updateChatSession(id, chatSessionDto));
    }
}

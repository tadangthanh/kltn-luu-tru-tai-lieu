package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AssistantFileRequest;
import vn.kltn.dto.response.AssistantFileDto;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IAssistantFileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant-file")
@Validated
public class AssistantFileRest {
    private final IAssistantFileService assistantFileService;

    @PostMapping
    public ResponseData<List<AssistantFileDto>> createAssistantFile(@Valid @RequestBody List<AssistantFileRequest> assistantFilesRequest) {
        return new ResponseData<>(201, "Thành công", assistantFileService.uploadFile(assistantFilesRequest));
    }


    @GetMapping("/{chatSessionId}")
    public ResponseData<List<AssistantFileDto>> getList(@PathVariable Long chatSessionId) {
        List<AssistantFileDto> pageResponse = assistantFileService.getListFileByChatSessionId(chatSessionId);
        return new ResponseData<>(200, "Thành công", pageResponse);
    }

    @DeleteMapping("/{name} ")
    public ResponseData<Void> deleteAssistantFile(@PathVariable String name) {
        assistantFileService.deleteByName(name);
        return new ResponseData<>(200, "Xóa thành công", null);
    }

    @PutMapping("/{name}")
    public ResponseData<AssistantFileDto> updateAssistantFile(@PathVariable String name, @Valid @RequestBody AssistantFileRequest assistantFileRequest) {
        return new ResponseData<>(200, "Cập nhật thành công", assistantFileService.update(name, assistantFileRequest));
    }
}

package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.AssistantFileDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.repository.util.FileUtil;
import vn.kltn.service.IAssistantFileService;
import vn.kltn.validation.ValidFiles;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant-file")
@Validated
public class AssistantFileRest {
    private final IAssistantFileService assistantFileService;

    @PostMapping
    public ResponseData<AssistantFileDto> createAssistantFile(@Valid @RequestBody AssistantFileDto assistantFileDto) {
        return new ResponseData<>(201, "Thành công", assistantFileService.createFile(assistantFileDto));
    }

    @GetMapping
    public ResponseData<PageResponse<List<AssistantFileDto>>> getPage(Pageable pageable) {
        PageResponse<List<AssistantFileDto>> pageResponse = assistantFileService.getFiles(pageable);
        return new ResponseData<>(200, "Thành công", pageResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteAssistantFile(@PathVariable Long id) {
        assistantFileService.delete(id);
        return new ResponseData<>(200, "Xóa thành công", null);
    }
}

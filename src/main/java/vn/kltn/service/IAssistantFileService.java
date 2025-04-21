package vn.kltn.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import vn.kltn.dto.AssistantFileDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.AssistantFile;

import java.util.List;

public interface IAssistantFileService {
    AssistantFileDto createFile(AssistantFileDto assistantFileDto);

    PageResponse<List<AssistantFileDto>> getFiles(Pageable pageable);

    AssistantFile getFileById(Long id);

    void deleteByName(String name);

    AssistantFileDto update(String name, AssistantFileDto assistantFileDto);
}

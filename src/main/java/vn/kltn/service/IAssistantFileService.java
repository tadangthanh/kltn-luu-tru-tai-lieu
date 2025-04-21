package vn.kltn.service;

import vn.kltn.dto.AssistantFileDto;
import vn.kltn.entity.AssistantFile;

import java.util.List;

public interface IAssistantFileService {
    AssistantFileDto uploadFile(AssistantFileDto assistantFileDto);

    List<AssistantFileDto> getListFileByChatSessionId(Long chatSessionId);

    AssistantFile getFileById(Long id);

    void deleteByName(String name);

    AssistantFileDto update(String name, AssistantFileDto assistantFileDto);
}

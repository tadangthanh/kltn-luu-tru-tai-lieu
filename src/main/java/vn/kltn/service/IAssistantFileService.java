package vn.kltn.service;

import vn.kltn.dto.request.AssistantFileRequest;
import vn.kltn.dto.response.AssistantFileDto;
import vn.kltn.entity.AssistantFile;
import vn.kltn.entity.ChatSession;

import java.util.List;

public interface IAssistantFileService {
    AssistantFileDto uploadFile(AssistantFileRequest assistantFileRequest);

    List<AssistantFileDto> getListFileByChatSessionId(Long chatSessionId);

    AssistantFile getFileById(Long id);

    void deleteByName(String name);

    AssistantFileDto update(String name, AssistantFileRequest assistantFileRequest);

    List<AssistantFile> save(ChatSession chatSession, List<AssistantFileDto> assistantFileDtoList);
}

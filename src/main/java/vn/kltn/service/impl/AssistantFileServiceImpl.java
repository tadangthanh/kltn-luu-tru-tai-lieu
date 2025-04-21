package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AssistantFileRequest;
import vn.kltn.dto.response.AssistantFileDto;
import vn.kltn.entity.AssistantFile;
import vn.kltn.entity.ChatSession;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.AssistantFileMapper;
import vn.kltn.repository.AssistantFileRepo;
import vn.kltn.repository.ChatSessionRepo;
import vn.kltn.service.IAssistantFileService;
import vn.kltn.service.IAuthenticationService;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CONVERSATION_FILE_SERVICE")
public class AssistantFileServiceImpl implements IAssistantFileService {
    private final AssistantFileMapper assistantFileMapper;
    private final AssistantFileRepo assistantFileRepo;
    private final IAuthenticationService authenticationService;
    private final ChatSessionRepo chatSessionRepo;

    @Override
    public AssistantFileDto uploadFile(AssistantFileRequest assistantFileRequest) {
        log.info("Create assistant file: {}", assistantFileRequest.getName());
        AssistantFile assistantFile = assistantFileMapper.toEntity(assistantFileRequest);
        assistantFile = assistantFileRepo.save(assistantFile);
        return assistantFileMapper.toResponse(assistantFile);
    }

    @Override
    public List<AssistantFileDto> getListFileByChatSessionId(Long chatSessionId) {
        log.info("Get assistant files by chat session id: {}", chatSessionId);
        User currentUser = authenticationService.getCurrentUser();
        List<AssistantFile> assistantFileList = assistantFileRepo.findAllByChatSessionId(chatSessionId, currentUser.getId());
        return assistantFileList.stream()
                .map(assistantFileMapper::toResponse)
                .toList();
    }

    @Override
    public AssistantFile getFileById(Long id) {
        return assistantFileRepo.findById(id).orElseThrow(() -> {
            log.error("File not found, id: {}", id);
            return new ResourceNotFoundException("File Assistant not found");
        });
    }

    @Override
    public void deleteByName(String name) {
        log.info("Delete assistant file: name={}", name);
        assistantFileRepo.deleteByName(name);
    }

    @Override
    public AssistantFileDto update(String name, AssistantFileRequest assistantFileRequest) {
        AssistantFile fileExist = assistantFileRepo.findByName(name).orElseThrow(() -> {
            log.error("Assistant file not found, name: {}", name);
            return new ResourceNotFoundException("Assistant file not found");
        });
        assistantFileMapper.updateEntity(fileExist, assistantFileRequest);
        return assistantFileMapper.toResponse(assistantFileRepo.save(fileExist));
    }

    @Override
    public List<AssistantFile> save(ChatSession chatSession, List<AssistantFileDto> assistantFileDtoList) {
        if (assistantFileDtoList == null || assistantFileDtoList.isEmpty()) {
            return new ArrayList<>();
        }

        List<AssistantFile> assistantFileList = assistantFileMapper.listToEntity(assistantFileDtoList);
        for (AssistantFile assistantFile : assistantFileList) {
            assistantFile.setChatSession(chatSession);
        }
        return assistantFileRepo.saveAll(assistantFileList);
    }

}

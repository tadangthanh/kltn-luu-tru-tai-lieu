package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.AssistantFileDto;
import vn.kltn.entity.AssistantFile;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.AssistantFileMapper;
import vn.kltn.repository.AssistantFileRepo;
import vn.kltn.service.IAssistantFileService;
import vn.kltn.service.IAuthenticationService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "CONVERSATION_FILE_SERVICE")
public class AssistantFileServiceImpl implements IAssistantFileService {
    private final AssistantFileMapper assistantFileMapper;
    private final AssistantFileRepo assistantFileRepo;
    private final IAuthenticationService authenticationService;

    @Override
    public AssistantFileDto uploadFile(AssistantFileDto assistantFileDto) {
        log.info("Create assistant file: {}", assistantFileDto.getName());
        AssistantFile assistantFile = assistantFileMapper.toEntity(assistantFileDto);
        assistantFile = assistantFileRepo.save(assistantFile);
        return assistantFileMapper.toDto(assistantFile);
    }

    @Override
    public List<AssistantFileDto> getListFileByChatSessionId(Long chatSessionId) {
        log.info("Get assistant files by chat session id: {}", chatSessionId);
        User currentUser = authenticationService.getCurrentUser();
        List<AssistantFile> assistantFileList = assistantFileRepo.findAllByChatSessionId(chatSessionId,currentUser.getId());
        return assistantFileList.stream()
                .map(assistantFileMapper::toDto)
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
    public AssistantFileDto update(String name, AssistantFileDto assistantFileDto) {
        AssistantFile fileExist = assistantFileRepo.findByName(name).orElseThrow(() -> {
            log.error("Assistant file not found, name: {}", name);
            return new ResourceNotFoundException("Assistant file not found");
        });
        assistantFileMapper.updateEntity(fileExist, assistantFileDto);
        return assistantFileMapper.toDto(assistantFileRepo.save(fileExist));
    }
}

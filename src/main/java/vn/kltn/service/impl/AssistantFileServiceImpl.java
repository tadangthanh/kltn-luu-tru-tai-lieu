package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.dto.AssistantFileDto;
import vn.kltn.dto.response.PageResponse;
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
    public AssistantFileDto createFile(AssistantFileDto assistantFileDto) {
        log.info("Create assistant file: {}", assistantFileDto.getName());
        AssistantFile assistantFile = assistantFileMapper.toEntity(assistantFileDto);
        assistantFile.setUser(authenticationService.getCurrentUser());
        assistantFile = assistantFileRepo.save(assistantFile);
        return assistantFileMapper.toDto(assistantFile);
    }

    @Override
    public PageResponse<List<AssistantFileDto>> getFiles(Pageable pageable) {
        log.info("Get assistant files page: {},size :{}", pageable.getPageNumber(), pageable.getPageSize());
        User currentUser = authenticationService.getCurrentUser();
        Page<AssistantFile> assistantFilePage = assistantFileRepo.findAllByCurrentUser(currentUser.getId(), pageable);
        List<AssistantFileDto> assistantFileList = assistantFilePage.getContent().stream()
                .map(assistantFileMapper::toDto)
                .toList();
        return PageResponse.<List<AssistantFileDto>>builder()
                .items(assistantFileList)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(assistantFilePage.getTotalPages())
                .totalItems(assistantFilePage.getTotalElements())
                .hasNext(assistantFilePage.hasNext())
                .build();
    }

    @Override
    public AssistantFile getFileById(Long id) {
        return assistantFileRepo.findById(id).orElseThrow(() -> {
            log.error("File not found, id: {}", id);
            return new ResourceNotFoundException("File Assistant not found");
        });
    }

    @Override
    public void delete(Long id) {
        log.info("Delete assistant file: id={}", id);
        assistantFileRepo.deleteById(id);
    }
}

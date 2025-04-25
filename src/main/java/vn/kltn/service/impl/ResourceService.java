package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.repository.FileSystemRepo;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RESOURCE_SERVICE")
public class ResourceService {
    private final IDocumentService documentService;
    private final IFolderService folderService;
    private final FileSystemRepo fileSystemRepo;

    public void getResource() {
        List<FileSystemEntity> fileSystemEntities = fileSystemRepo.findAll();
        for (FileSystemEntity fileSystemEntity : fileSystemEntities) {
            System.out.println("fileSystemEntity = " + fileSystemEntity.getName());
        }
    }
}

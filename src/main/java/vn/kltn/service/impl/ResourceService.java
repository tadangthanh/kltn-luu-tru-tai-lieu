package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Item;
import vn.kltn.repository.ItemRepo;
import vn.kltn.service.IDocumentCommonService;
import vn.kltn.service.IFolderCommonService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RESOURCE_SERVICE")
public class ResourceService {
    private final IDocumentCommonService documentService;
    private final IFolderCommonService folderService;
    private final ItemRepo itemRepo;

    public void getResource() {
        List<Item> fileSystemEntities = itemRepo.findAll();
        for (Item fileSystemEntity : fileSystemEntities) {
            System.out.println("fileSystemEntity = " + fileSystemEntity.getName());
        }
    }
}

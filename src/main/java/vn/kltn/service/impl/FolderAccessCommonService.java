package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.FolderAccess;
import vn.kltn.repository.FolderAccessRepo;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_ACCESS_COMMON_SERVICE")
public class FolderAccessCommonService {
    private final FolderAccessRepo folderAccessRepo;

    public Set<FolderAccess> getFolderAccessByResourceId(Long resourceId) {
        return folderAccessRepo.findAllByResourceId(resourceId);
    }
}

package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IAuthenticationService;

@Service
@Transactional
@Slf4j(topic = "FOLDER_COMMON_SERVICE")
@RequiredArgsConstructor
public class FolderCommonService extends AbstractResourceCommonService<Folder> {
    private final IAuthenticationService authenticationService;
    private final FolderRepo folderRepo;

    @Override
    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }

    @Override
    protected Folder getResourceByIdOrThrow(Long resourceId) {
        return folderRepo.findById(resourceId).orElseThrow(() -> {
            log.warn("Folder with id {} not found", resourceId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }
}

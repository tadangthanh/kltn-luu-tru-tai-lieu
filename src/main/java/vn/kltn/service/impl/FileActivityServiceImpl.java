package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.FileActionType;
import vn.kltn.entity.File;
import vn.kltn.entity.FileActivity;
import vn.kltn.entity.User;
import vn.kltn.repository.FileActivityRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFileActivityService;
import vn.kltn.service.IFileService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FILE_ACTIVITY_SERVICE")
public class FileActivityServiceImpl implements IFileActivityService {
    private final FileActivityRepo fileActivityRepo;
    private final IAuthenticationService authService;
    private final IFileService fileService;

    @Override
    public void logActivity(Long fileId, FileActionType action, String detail) {
        log.info("Log activity for repoId: {}, action: {}, detail: {}", fileId, action, detail);
        File file = fileService.getFileById(fileId);
        saveActivity(file, action, detail);
    }

    private void saveActivity(File file, FileActionType action, String detail) {
        FileActivity activity = new FileActivity();
        User authUser = authService.getAuthUser();
        activity.setFile(file);
        activity.setUser(authUser);
        activity.setAction(action);
        activity.setDetails(detail);
        fileActivityRepo.save(activity);
    }

    @Override
    public void deleteActivitiesByRepoId(Long fileId) {
        log.info("Delete activities by repoId: {}", fileId);
        fileActivityRepo.deleteByFileId(fileId);
    }
}

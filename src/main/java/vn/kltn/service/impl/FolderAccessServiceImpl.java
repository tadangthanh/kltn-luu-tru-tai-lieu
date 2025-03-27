package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.FolderAccessResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FolderAccessMapper;
import vn.kltn.repository.FolderAccessRepo;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_ACCESS_SERVICE")
public class FolderAccessServiceImpl extends AbstractAccessService<FolderAccess, FolderAccessResponse> {
    private final FolderAccessRepo folderAccessRepo;
    private final FolderAccessMapper folderAccessMapper;
    private final IUserService userService;
    private final IMailService mailService;
    private final ResourceCommonService resourceCommonService;


    private void validateFolderConditionsAccess(Folder folder) {
        resourceCommonService.validateResourceNotDeleted(folder);
        resourceCommonService.validateCurrentUserIsOwnerResource(folder);
    }

    @Override
    protected Page<FolderAccess> getPageAccessByResource(Pageable pageable) {
        return folderAccessRepo.findAll(pageable);
    }

    @Override
    protected Page<FolderAccess> getPageAccessByResourceBySpec(Specification<FolderAccess> spec, Pageable pageable) {
        return folderAccessRepo.findAll(spec, pageable);
    }

    @Override
    protected FolderAccessResponse mapToR(FolderAccess access) {
        return folderAccessMapper.toFolderAccessResponse(access);
    }

    @Override
    protected void sendEmailInviteAccess(FolderAccess access, AccessRequest accessRequest) {
        mailService.sendEmailInviteFolderAccess(accessRequest.getRecipientEmail(), access, accessRequest.getMessage());
    }

    @Override
    protected FolderAccess createEmptyAccess() {
        return new FolderAccess();
    }

    @Override
    protected void setResource(FolderAccess access, Long resourceId) {
        Folder folder = resourceCommonService.getFolderByIdOrThrow(resourceId);
        validateFolderConditionsAccess(folder);
        access.setResource(folder);
    }

    @Override
    protected FolderAccess findAccessById(Long accessId) {
        return folderAccessRepo.findById(accessId).orElseThrow(() -> {
            log.warn("Folder access not found by id: {}", accessId);
            return new ResourceNotFoundException("Folder access not found");
        });
    }

    @Override
    protected FolderAccess saveAccess(FolderAccess access) {
        return folderAccessRepo.save(access);
    }

    @Override
    protected void deleteAccessEntity(FolderAccess access) {
        folderAccessRepo.delete(access);
    }

    @Override
    protected User getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }
}

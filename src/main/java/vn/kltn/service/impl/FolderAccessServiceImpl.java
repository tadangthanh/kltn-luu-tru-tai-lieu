package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.AccessResourceMapper;
import vn.kltn.repository.FolderAccessRepo;
import vn.kltn.service.IFolderAccessService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_ACCESS_SERVICE")
public class FolderAccessServiceImpl extends AbstractAccessService<FolderAccess, AccessResourceResponse> implements IFolderAccessService {
    private final FolderAccessRepo folderAccessRepo;
    private final AccessResourceMapper accessResourceMapper;
    private final IUserService userService;
    private final IMailService mailService;
    private final FolderCommonService folderCommonService;


    private void validateFolderConditionsAccess(Folder folder) {
        folderCommonService.validateResourceNotDeleted(folder);
        folderCommonService.validateCurrentUserIsOwnerResource(folder);
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
    protected AccessResourceResponse mapToR(FolderAccess access) {
        return accessResourceMapper.toAccessResourceResponse(access);
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
        Folder folder = folderCommonService.getResourceById(resourceId);
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

    @Override
    public Set<FolderAccess> getAllByResourceId(Long resourceId) {
        return folderAccessRepo.findAllByResourceId(resourceId);
    }
}

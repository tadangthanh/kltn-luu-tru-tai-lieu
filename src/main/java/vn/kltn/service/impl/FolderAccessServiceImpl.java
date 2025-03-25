package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.FolderAccessResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;
import vn.kltn.entity.User;
import vn.kltn.map.FolderAccessMapper;
import vn.kltn.repository.FolderAccessRepo;
import vn.kltn.service.IFolderAccessService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_ACCESS_SERVICE")
public class FolderAccessServiceImpl implements IFolderAccessService {
    private final FolderAccessRepo folderAccessRepo;
    private final FolderAccessMapper folderAccessMapper;
    private final IUserService userService;
    private final IMailService mailService;
    private final FolderCommonService folderCommonService;


    @Override
    public FolderAccessResponse createFolderAccess(Long folderId, AccessRequest accessRequest) {
        Folder folderToAccess = folderCommonService.getFolderByIdOrThrow(folderId);
        // folder chua bi xoa, nguoi chia se phai la chu so huu cua folder
        validateFolderConditionsToAccess(folderToAccess);
        FolderAccess folderAccessSaved = saveFolderAccess(folderToAccess, accessRequest);
        sendEmailInviteFolderAccess(folderAccessSaved, accessRequest);
        return mapToFolderAccessResponse(folderAccessSaved);
    }

    private void sendEmailInviteFolderAccess(FolderAccess folderAccess, AccessRequest accessRequest) {
        mailService.sendEmailInviteFolderAccess(accessRequest.getRecipientEmail(), folderAccess, accessRequest.getMessage());
    }

    private void validateFolderConditionsToAccess(Folder folder) {
        folderCommonService.validateFolderNotDeleted(folder);
        folderCommonService.validateCurrentUserIsOwnerFolder(folder);
    }

    private FolderAccessResponse mapToFolderAccessResponse(FolderAccess folderAccess) {
        return folderAccessMapper.toFolderAccessResponse(folderAccess);
    }

    private FolderAccess saveFolderAccess(Folder folderToAccess, AccessRequest accessRequest) {
        FolderAccess folderAccess = new FolderAccess();
        folderAccess.setFolder(folderToAccess);
        folderAccess.setPermission(accessRequest.getPermission());
        User recipient = userService.getUserByEmail(accessRequest.getRecipientEmail());
        folderAccess.setRecipient(recipient);
        return folderAccessRepo.save(folderAccess);
    }

    @Override
    public void deleteFolderAccess(Long folderId, Long recipientId) {

    }
}

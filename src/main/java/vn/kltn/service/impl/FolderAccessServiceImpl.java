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
import vn.kltn.service.IFolderService;
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
    private final IFolderService folderService;


    @Override
    public FolderAccessResponse createFolderAccess(Long folderId, AccessRequest accessRequest) {
        FolderAccess folderAccess = mapToFolderAccess(folderId, accessRequest);
        folderAccess = folderAccessRepo.save(folderAccess);
        mailService.sendEmailInviteFolderAccess(accessRequest.getRecipientEmail(), folderAccess,accessRequest.getMessage());
        return mapToFolderAccessResponse(folderAccess);
    }

    private FolderAccessResponse mapToFolderAccessResponse(FolderAccess folderAccess) {
        return folderAccessMapper.toFolderAccessResponse(folderAccess);
    }

    private FolderAccess mapToFolderAccess(Long folderId, AccessRequest accessRequest) {
        FolderAccess folderAccess = new FolderAccess();
        Folder folder = folderService.getFolderByIdOrThrow(folderId);
        folderAccess.setFolder(folder);
        folderAccess.setPermission(accessRequest.getPermission());
        User recipient = userService.getUserByEmail(accessRequest.getRecipientEmail());
        folderAccess.setRecipient(recipient);
        return folderAccess;
    }

    @Override
    public void deleteFolderAccess(Long folderId, Long recipientId) {

    }
}

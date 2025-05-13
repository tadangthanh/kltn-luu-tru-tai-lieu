package vn.kltn.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.common.CancellationToken;
import vn.kltn.common.ItemType;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.FolderContent;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl extends AbstractItemCommonService<Folder, FolderResponse> implements IFolderService {
    private final FolderRepo folderRepo;
    private final FolderCommonService folderCommonService;
    private final IFolderCreationService folderCreationService;
    private final IFolderMapperService folderMapperService;
    private final IFolderDeletionService folderDeletionService;
    private final IFolderRestorationService folderRestorationService;
    private final ItemValidator itemValidator;
    private final DocumentRepo documentRepo;
    private final IItemIndexService itemIndexService;
    private final IDocumentService documentService;

    public FolderServiceImpl(FolderRepo folderRepo, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IFolderCreationService folderCreationService, IFolderMapperService folderMapperService, IFolderDeletionService folderDeletionService, IFolderRestorationService folderRestorationService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionService permissionService, IPermissionValidatorService permissionValidatorService, DocumentRepo documentRepo, IItemIndexService itemIndexService, IDocumentService documentService) {
        super(authenticationService, folderCommonService, itemValidator, permissionInheritanceService, permissionValidatorService, permissionService);
        this.folderRepo = folderRepo;
        this.folderCommonService = folderCommonService;
        this.folderCreationService = folderCreationService;
        this.folderMapperService = folderMapperService;
        this.folderDeletionService = folderDeletionService;
        this.folderRestorationService = folderRestorationService;
        this.itemValidator = itemValidator;
        this.documentRepo = documentRepo;
        this.itemIndexService = itemIndexService;
        this.documentService = documentService;
    }

    @Override
    public FolderResponse createFolder(FolderRequest folderRequest) {
        Folder folder = folderCreationService.createFolder(folderRequest);
        itemIndexService.insertItem(folder);
        return mapToR(folder);
    }

    @Override
    public Folder getFolderByIdOrThrow(Long folderId) {
        return folderCommonService.getFolderByIdOrThrow(folderId);
    }

    @Override
    protected Folder saveResource(Folder resource) {
        return folderRepo.save(resource);
    }

    @Override
    public void softDeleteFolderById(Long folderId) {
        log.info("Soft delete folder: folderId={}", folderId);
        Folder folder = getFolderByIdOrThrow(folderId);
        folderDeletionService.softDelete(folder);
    }

    @Override
    public FolderResponse restoreItemById(Long itemId) {
        Folder folder = folderRestorationService.restore(itemId);
        return folderMapperService.mapToResponse(folder);
    }

    @Override
    public Folder getItemByIdOrThrow(Long itemId) {
        return folderRepo.findById(itemId).orElseThrow(() -> {
            log.warn("Folder with id {} is not found", itemId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }


    @Override
    public void hardDeleteFolderById(Long folderId) {
        Folder folder = getFolderByIdOrThrow(folderId);
        folderDeletionService.hardDelete(folder);
        itemIndexService.deleteDocById(folderId);
    }

    @Override
    @Async("taskExecutor")
    public void uploadFolderNullParent(List<FileBuffer> fileBufferList, CancellationToken token) {
        Map<String, Folder> folderCache = new HashMap<>();
        Map<Long, List<FileBuffer>> groupedByParentId = new HashMap<>();
        List<FileBuffer> rootFiles = new ArrayList<>();
        User currentUser = authenticationService.getCurrentUser();

        for (FileBuffer fileBuffer : fileBufferList) {
            String filePath = fileBuffer.getFileName(); // vd: nhom10/Nhóm 10/abc.txt
            Path path = Paths.get(filePath);

            Folder parent = null;
            StringBuilder currentPath = new StringBuilder();

            // Tạo folder cha theo path
            for (int i = 0; i < path.getNameCount() - 1; i++) {
                String folderName = path.getName(i).toString();
                if (!currentPath.isEmpty()) currentPath.append("/");
                currentPath.append(folderName);
                String fullPath = currentPath.toString();

                Folder folder = folderCache.get(fullPath);
                if (folder == null) {
                    Folder finalParent = parent;
                    folder = folderRepo.findByNameAndParent(folderName, parent)
                            .orElseGet(() -> {
                                Folder newFolder = new Folder();
                                newFolder.setName(folderName);
                                newFolder.setParent(finalParent);
                                newFolder.setItemType(ItemType.FOLDER);
                                newFolder.setOwner(currentUser);
                                return folderRepo.saveAndFlush(newFolder);
                            });
                    folderCache.put(fullPath, folder);
                }
                parent = folder;
            }

            // Sửa tên file trong buffer
            String fileName = path.getFileName().toString();
            FileBuffer fixedBuffer = new FileBuffer(fileName,fileBuffer.getData(), fileBuffer.getSize(),fileBuffer.getContentType());

            if (parent == null) {
                rootFiles.add(fixedBuffer);
            } else {
                groupedByParentId.computeIfAbsent(parent.getId(), k -> new ArrayList<>()).add(fixedBuffer);
            }
        }

        // Upload các file không có folder cha
        if (!rootFiles.isEmpty()) {
            documentService.uploadDocumentNullParentBlocking(rootFiles, token);
        }

        // Upload từng nhóm theo folder cha
        for (Map.Entry<Long, List<FileBuffer>> entry : groupedByParentId.entrySet()) {
            documentService.uploadDocumentWithParentBlocking(entry.getKey(), entry.getValue(), token);
        }

        log.info("Upload folder hoàn tất tất cả file.");
    }

    @Override
    @Async("taskExecutor")
    public void uploadFolderWithParent(Long folderId, List<FileBuffer> fileBufferList, CancellationToken token) {
        Map<String, Folder> folderCache = new HashMap<>();
        Map<Long, List<FileBuffer>> groupedByParentId = new HashMap<>();
        User currentUser = authenticationService.getCurrentUser();

        // Lấy folder cha gốc
        Folder rootParent = folderRepo.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder cha không tồn tại"));

        for (FileBuffer fileBuffer : fileBufferList) {
            String filePath = fileBuffer.getFileName(); // vd: Nhóm 10/abc.txt
            Path path = Paths.get(filePath);

            Folder parent = rootParent; // bắt đầu từ folder cha được truyền vào
            StringBuilder currentPath = new StringBuilder();

            // tạo folder theo path con (nếu có)
            for (int i = 0; i < path.getNameCount() - 1; i++) {
                String folderName = path.getName(i).toString();
                if (!currentPath.isEmpty()) currentPath.append("/");
                currentPath.append(folderName);
                String fullPath = currentPath.toString();

                Folder folder = folderCache.get(fullPath);
                if (folder == null) {
                    Folder finalParent = parent;
                    folder = folderRepo.findByNameAndParent(folderName, parent)
                            .orElseGet(() -> {
                                Folder newFolder = new Folder();
                                newFolder.setName(folderName);
                                newFolder.setParent(finalParent);
                                newFolder.setItemType(ItemType.FOLDER);
                                newFolder.setOwner(currentUser);
                                return folderRepo.saveAndFlush(newFolder);
                            });
                    folderCache.put(fullPath, folder);
                }
                parent = folder;
            }

            // Sửa lại tên file gốc
            String fileName = path.getFileName().toString();
            FileBuffer fixedBuffer = new FileBuffer(fileName, fileBuffer.getData(), fileBuffer.getSize(), fileBuffer.getContentType());

            groupedByParentId.computeIfAbsent(parent.getId(), k -> new ArrayList<>()).add(fixedBuffer);
        }

        // Upload từng nhóm file theo folder cha
        for (Map.Entry<Long, List<FileBuffer>> entry : groupedByParentId.entrySet()) {
            documentService.uploadDocumentWithParentBlocking(entry.getKey(), entry.getValue(), token);
        }

        log.info("Upload folder (có parent) hoàn tất tất cả file.");
    }



    @Override
    protected Page<Folder> getPageResourceBySpec(Specification<Folder> spec, Pageable pageable) {
        return folderRepo.findAll(spec, pageable);
    }

    @Override
    protected FolderResponse mapToR(Folder resource) {
        return folderMapperService.mapToResponse(resource);
    }

    @Override
    public FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest) {
        log.info("update folder with id: {}", folderId);
        Folder folder = getFolderByIdOrThrow(folderId);
        itemValidator.validateCurrentUserIsOwnerItem(folder);
        itemValidator.validateItemNotDeleted(folder);
        folderMapperService.updateFolder(folder, folderRequest);
        itemIndexService.syncItem(folderId);
        return folderMapperService.mapToResponse(folderRepo.save(folder));
    }

    @Override
    public List<FolderContent> getAllContents(Long folderId, String currentPath) {
        List<FolderContent> results = new ArrayList<>();
        results.add(new FolderContent(currentPath + "/", true, null));
        // Lấy tất cả document của folder này
        List<Document> documents = documentRepo.findByParentId(folderId);
        for (Document doc : documents) {
            results.add(new FolderContent(
                    currentPath + "/" + doc.getName(),
                    false,
                    doc.getCurrentVersion().getBlobName()
            ));
        }

        // Lấy tất cả folder con
        List<Folder> subFolders = folderRepo.findByParentId(folderId);
        for (Folder sub : subFolders) {
            results.add(new FolderContent(
                    currentPath + "/" + sub.getName() + "/",
                    true,
                    null
            ));
            // đệ quy xuống folder con
            results.addAll(getAllContents(sub.getId(), currentPath + "/" + sub.getName()));
        }

        return results;
    }

}

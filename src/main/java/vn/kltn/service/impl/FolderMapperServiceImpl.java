package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;
import vn.kltn.map.FolderMapper;
import vn.kltn.service.IFolderMapperService;
@Service
@Slf4j(topic = "FOLDER_MAPPER_SERVICE")
@RequiredArgsConstructor
public class FolderMapperServiceImpl implements IFolderMapperService {
    private final FolderMapper folderMapper;

    @Override
    public Folder mapToFolder(FolderRequest folderRequest) {
        return folderMapper.toFolder(folderRequest);
    }

    @Override
    public FolderResponse mapToResponse(Folder folder) {
        FolderResponse folderResponse = folderMapper.toFolderResponse(folder);
        if (folder.getParent() != null) {
            folderResponse.setParentId(folder.getParent().getId());
        }
        return folderResponse;
    }

    @Override
    public void updateFolder(Folder folder, FolderRequest folderRequest) {
        folderMapper.updateFolderFromRequest(folderRequest, folder);
    }
}

package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FolderMapper {
    Folder toFolder(FolderRequest folderRequest);

    @Mapping(target = "ownerName", source = "user.fullName")
    @Mapping(target = "ownerEmail", source = "user.email")
    FolderResponse toFolderResponse(Folder folder);

    void updateFolderFromRequest(FolderRequest folderRequest, @MappingTarget Folder folder);
}

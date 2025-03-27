package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.FolderAccessResponse;
import vn.kltn.entity.FolderAccess;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", uses = {UserMapper.class, FolderMapper.class}, nullValuePropertyMappingStrategy = IGNORE)
public interface FolderAccessMapper {
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
    @Mapping(target = "owner", source = "resource.owner")
    FolderAccessResponse toFolderAccessResponse(FolderAccess folderAccess);
}

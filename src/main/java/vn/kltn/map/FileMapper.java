package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.entity.File;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileMapper {

    @Mapping(target = "repoName", source = "repo.name")
    @Mapping(target = "repoId", source = "repo.id")
    @Mapping(target = "uploadedBy", source = "uploadedBy.user.fullName")
    @Mapping(target = "isPublic", source = "public")
    FileResponse entityToResponse(File file);

    @Mapping(target = "public", source = "isPublic")
    File requestToEntity(FileRequest request);

    @Mapping(target = "public", source = "isPublic")
    void updateEntity(FileRequest request, @MappingTarget File file);
}

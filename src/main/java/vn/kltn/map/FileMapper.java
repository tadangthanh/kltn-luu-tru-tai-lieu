package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.entity.File;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileMapper {

    @Mapping(target = "repoName", source = "repo.name")
    @Mapping(target = "repoId", source = "repo.id")
    @Mapping(target = "uploadedBy", source = "uploadedBy.user.fullName")
    FileResponse entityToResponse(File file);

    File requestToEntity(FileRequest request);
}

package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.entity.File;
import vn.kltn.entity.FileShare;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileShareMapper {
    FileShare toEntity(FileShareRequest fileShareRequest);

    @Mapping(target = "fileName",source = "file.fileName")
    @Mapping(target = "fileId",source = "file.id")
    FileShareResponse toResponse(FileShare fileShare);

}

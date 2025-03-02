package vn.kltn.map;

import org.mapstruct.Mapper;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.entity.FileShare;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileShareMapper {
    FileShare toEntity(FileShareRequest fileShareRequest);

    FileShareResponse toResponse(FileShare fileShare);
}

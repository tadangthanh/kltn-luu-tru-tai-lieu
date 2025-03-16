package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.FileActivityResponse;
import vn.kltn.entity.FileActivity;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileActivityMapper {
    @Mapping(target = "fileName", source = "file.fileName")
    @Mapping(target = "fileId", source = "file.id")
    @Mapping(target = "authorName", source = "user.fullName")
    @Mapping(target = "authorEmail", source = "user.email")
    @Mapping(target = "authorId", source = "user.id")
    FileActivityResponse toResponse(FileActivity entity);
}

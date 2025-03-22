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
    @Mapping(target = "memberName", source = "member.user.fullName")
    @Mapping(target = "memberEmail", source = "member.user.email")
    @Mapping(target = "memberId", source = "member.id")
    FileActivityResponse toResponse(FileActivity entity);
}

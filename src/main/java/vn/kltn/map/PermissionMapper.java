package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.Permission;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface PermissionMapper {
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
    PermissionResponse toPermissionResponse(Permission permission);

}

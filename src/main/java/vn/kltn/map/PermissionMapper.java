package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.Permission;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface PermissionMapper {
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
    PermissionResponse toPermissionResponse(Permission permission);

    Permission toPermission(PermissionRequest permissionRequest);

    List<Permission> toListPermission(List<PermissionRequest> permissionRequests);

    @Mapping(target = "email", source = "recipient.email")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "userId", source = "recipient.id")
    ItemPermissionResponse toItemPermissionResponse(Permission permission);

    List<ItemPermissionResponse> toListItemPermissionResponse(List<Permission> permissions);
}

package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class ItemPermissionResponse extends BaseDto {
    private Permission permission;
    private String email;
    private Long userId;
    private Long itemId;
}

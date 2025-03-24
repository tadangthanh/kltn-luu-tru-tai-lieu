package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class DocumentAccessResponse extends BaseDto {
    private Permission permission;
    private String recipientName;
    private String recipientEmail;
    private Long documentId;
    private UserResponse owner;
}

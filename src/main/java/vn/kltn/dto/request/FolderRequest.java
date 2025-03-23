package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.validation.Create;

@Getter
@Setter
public class FolderRequest {
    @NotBlank(message = "Tên thư mục không được để trống", groups = {Create.class})
    private String name;
    private String description;
    private Long folderParentId;
}

package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.index.BaseSearchEntity;

@Getter
@Setter
public class ItemIndexResponse extends BaseSearchEntity {
    private Long itemId; // ID của item
    private String name; // tên item
    private String docType; // loại tài liệu
    private String itemType; // loại tài liệu (folder, file)
    private String content; // Full nội dung text nếu là của tài liệu
    private Long ownerId;
    private boolean isDeleted = false;
    private boolean isSharedWithMe; // có phải là tài liệu được chia sẻ với tôi hay không
}

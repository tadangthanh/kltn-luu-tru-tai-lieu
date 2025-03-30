package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

@Getter
@Setter
@Entity
@Table(name = "document_access")
public class DocumentAccess extends BaseEntity implements AccessResource {
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Document resource;
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient; //nguời được cấp quyền truy cập
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Permission permission;
    // true: quyền truy cập tùy chỉnh, false: quyền truy cập mặc định kế thừa từ folder cha
    // khi update quyền truy cập từ folder cha thì kiểm tra xem quyền truy cập này có phải là quyền truy cập tùy chỉnh hay không
    //nếu là quyền truy cập tùy chỉnh thì không update quyền truy cập này
    private boolean isCustomPermission;

    @Override
    public <T extends Resource> void setResource(T resource) {
        this.resource = (Document) resource;
    }
}

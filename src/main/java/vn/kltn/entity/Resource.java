package vn.kltn.entity;

import java.time.LocalDateTime;
import java.util.Set;

public interface Resource {
    User getOwner();

    Long getId();

    Resource getParent();

    Set<Permission> getPermissions();

    void setParent(Resource parent);

    void setOwner(User owner);

    LocalDateTime getDeletedAt();

    void setDeletedAt(LocalDateTime deletedAt);

    LocalDateTime getPermanentDeleteAt();

    void setPermanentDeleteAt(LocalDateTime permanentDeleteAt);
}

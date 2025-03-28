package vn.kltn.entity;

import java.time.LocalDateTime;

public interface Resource {
    User getOwner();

    Long getId();

    Resource getParent();

    void setParent(Resource parent);

    void setOwner(User owner);

    LocalDateTime getDeletedAt();

    void setDeletedAt(LocalDateTime deletedAt);

    LocalDateTime getPermanentDeleteAt();

    void setPermanentDeleteAt(LocalDateTime permanentDeleteAt);
}

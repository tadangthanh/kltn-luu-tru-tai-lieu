package vn.kltn.entity;

import java.time.LocalDateTime;

public interface Resource {
    User getOwner();
    void setOwner(User owner);
    LocalDateTime getDeletedAt();
}

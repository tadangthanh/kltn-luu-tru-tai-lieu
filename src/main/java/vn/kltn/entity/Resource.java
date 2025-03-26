package vn.kltn.entity;

import java.time.LocalDateTime;

public interface Resource {
    User getOwner();
    LocalDateTime getDeletedAt();
}

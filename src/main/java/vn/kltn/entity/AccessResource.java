package vn.kltn.entity;

import vn.kltn.common.Permission;

public interface AccessResource {
    User getRecipient();

    void setRecipient(User recipient);

    Permission getPermission();

    void setPermission(vn.kltn.common.Permission permission);

    Resource getResource();

    <T extends Resource> void setResource(T resource);
}

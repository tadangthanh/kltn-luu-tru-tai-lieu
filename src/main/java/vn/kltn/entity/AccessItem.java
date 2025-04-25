package vn.kltn.entity;

import vn.kltn.common.Permission;

public interface AccessItem {
    User getRecipient();

    void setRecipient(User recipient);

    Permission getPermission();

    void setPermission(vn.kltn.common.Permission permission);

    Item getItem();

    <T extends Item> void setItem(T item);
}

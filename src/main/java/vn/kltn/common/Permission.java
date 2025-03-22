package vn.kltn.common;

import lombok.Getter;

@Getter
public enum Permission {
    VIEWER("Nguời xem"),
    EDITOR("Nguời chỉnh sửa");
    private final String description;

    Permission(String description) {
        this.description = description;
    }
}
package vn.kltn.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum Permission {
    @JsonProperty("viewer")
    VIEWER("Nguời xem"),
    @JsonProperty("editor")
    EDITOR("Nguời chỉnh sửa");
    private final String description;

    Permission(String description) {
        this.description = description;
    }
}
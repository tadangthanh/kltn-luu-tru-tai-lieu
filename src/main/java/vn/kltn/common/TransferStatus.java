package vn.kltn.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum TransferStatus {
    @JsonProperty("pending")
    PENDING("Đang chờ xử lý"),
    @JsonProperty("accepted")
    ACCEPTED("Đã chấp nhận"),
    @JsonProperty("declined")
    DECLINED("Đã từ chối");

    private final String description;

    TransferStatus(String description) {
        this.description = description;
    }
}

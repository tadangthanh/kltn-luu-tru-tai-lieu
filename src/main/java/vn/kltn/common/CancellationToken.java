package vn.kltn.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellationToken {
    @Getter
    private volatile boolean cancelled = false;
    private String uploadId;

    public void cancel() {
        this.cancelled = true;
    }
}

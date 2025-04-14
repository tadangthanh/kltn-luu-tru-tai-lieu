package vn.kltn.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellationToken {
    private volatile boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}

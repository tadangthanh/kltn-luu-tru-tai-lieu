package vn.kltn.util;

import java.util.Arrays;

public class RetryUtil {
    @SafeVarargs
    public static void runWithRetry(Runnable task, int maxRetries, long delayMillis, Class<? extends Throwable>... retryOn) {
        int attempts = 0;
        while (true) {
            try {
                task.run();
                return; // Thành công, thoát
            } catch (Throwable ex) {
                attempts++;
                boolean shouldRetry = Arrays.stream(retryOn).anyMatch(clazz -> clazz.isAssignableFrom(ex.getClass()));
                if (!shouldRetry || attempts > maxRetries) {
                    throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
                }
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread bị ngắt trong lúc retry", ie);
                }
            }
        }
    }
}

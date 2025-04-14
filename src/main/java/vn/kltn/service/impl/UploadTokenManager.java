package vn.kltn.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.kltn.common.CancellationToken;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j(topic = "UPLOAD_TOKEN_MANAGER")
public class UploadTokenManager {

    private final Cache<String, CancellationToken> tokenCache;

    public UploadTokenManager() {
        this.tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // TTL sau 10 phút tự remove
                .removalListener(notification -> {
                    log.info(" Token {} auto-removed (reason: {})",
                            notification.getKey(), notification.getCause());
                })
                .build();
    }

    public void registerToken(String uploadId, CancellationToken token) {
        tokenCache.put(uploadId, token);
        log.info(" Registered token: {}", uploadId);
    }

    public Optional<CancellationToken> getToken(String uploadId) {
        return Optional.ofNullable(tokenCache.getIfPresent(uploadId));
    }

    public void removeToken(String uploadId) {
        tokenCache.invalidate(uploadId);
        log.info("🗑 Manually removed token: {}", uploadId);
    }

    public boolean isRegistered(String uploadId) {
        return tokenCache.getIfPresent(uploadId) != null;
    }
}

package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.entity.Document;
import vn.kltn.entity.Item;
import vn.kltn.index.ItemIndex;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "UPLOAD_FINALIZER_SERVICE")
public class UploadFinalizerService {
    private final UploadTokenManager uploadTokenManager;
    private final UploadCleanupService uploadCleanupService;

    //Xóa token + cleanup file/db/index nếu user cancel
    public void finalizeUpload(CancellationToken token, List<Item> items,
                               List<ItemIndex> documentIndices, List<String> blobNames) {
        try {
            if (token.isCancelled()) {
                log.info(" Upload bị hủy, bắt đầu dọn dẹp...");
                uploadCleanupService.cleanupUpload(items, documentIndices, blobNames);
            }
        } finally {
            uploadTokenManager.removeToken(token.getUploadId());
            log.info(" Token [{}] đã được remove", token.getUploadId());
        }
    }

    //Chỉ xóa token (khi không cần cleanup)
    public void finalizeUpload(CancellationToken token) {
        uploadTokenManager.removeToken(token.getUploadId());
        log.info("Token [{}] đã được remove", token.getUploadId());
    }
    public boolean checkCancelledAndFinalize(CancellationToken token, List<Item> items,
                                             List<ItemIndex> indices, List<String> blobNames) {
        if (token.isCancelled()) {
            log.info("Upload was cancelled");
            finalizeUpload(token, items, indices, blobNames);
            return true;
        }
        return false;
    }

}

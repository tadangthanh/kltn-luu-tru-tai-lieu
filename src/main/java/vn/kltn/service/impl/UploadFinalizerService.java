package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.entity.Document;
import vn.kltn.index.DocumentIndex;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "UPLOAD_FINALIZER_SERVICE")
public class UploadFinalizerService {
    private final UploadTokenManager uploadTokenManager;
    private final UploadCleanupService uploadCleanupService;

    //Xóa token + cleanup file/db/index nếu user cancel
    public void finalizeUpload(CancellationToken token, List<Document> documents,
                               List<DocumentIndex> documentIndices, List<String> blobNames) {
        try {
            if (token.isCancelled()) {
                log.info(" Upload bị hủy, bắt đầu dọn dẹp...");
                uploadCleanupService.cleanupUpload(documents, documentIndices, blobNames);
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
    public boolean checkCancelledAndFinalize(CancellationToken token, List<Document> documents,
                                              List<DocumentIndex> indices, List<String> blobNames) {
        if (token.isCancelled()) {
            log.info("Upload was cancelled");
            finalizeUpload(token, documents, indices, blobNames);
            return true;
        }
        return false;
    }

}

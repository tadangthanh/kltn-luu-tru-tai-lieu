package vn.kltn.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.kltn.service.IDocumentIndexService;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class DocumentUpdatedEventListener {
    private final IDocumentIndexService documentIndexService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async("taskExecutor")
    public void handleDocumentUpdated(DocumentUpdatedEvent event) {
        Long docId = event.getDocumentId();
        documentIndexService.syncDocument(docId);
    }
}

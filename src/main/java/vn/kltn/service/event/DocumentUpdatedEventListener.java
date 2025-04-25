package vn.kltn.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.kltn.service.IDocumentIndexService;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class DocumentUpdatedEventListener {
    private final IDocumentIndexService documentIndexService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleDocumentUpdated(DocumentUpdatedEvent event)  {
        Long docId = event.getDocumentId();
        documentIndexService.syncDocument(docId);
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMultipleDocumentsUpdated(MultipleDocumentsUpdatedEvent event) {
        documentIndexService.syncDocuments(event.getDocumentIds());
    }
}

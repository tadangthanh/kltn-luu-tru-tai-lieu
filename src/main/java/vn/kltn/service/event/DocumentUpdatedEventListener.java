package vn.kltn.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import vn.kltn.service.IDocumentIndexService;

@Component
@RequiredArgsConstructor
public class DocumentUpdatedEventListener {
    private final IDocumentIndexService documentIndexService;

    @EventListener
    public void handleDocumentUpdated(DocumentUpdatedEvent event) {
        Long docId = event.getDocumentId();
        documentIndexService.syncDocument(docId);
    }
}

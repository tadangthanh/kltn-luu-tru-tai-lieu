package vn.kltn.service.event.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import vn.kltn.service.event.DocumentUpdateContent;
import vn.kltn.service.event.ItemUpdatedEvent;
import vn.kltn.service.event.MultipleItemsUpdatedEvent;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DocumentEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishDocumentsUpdate(Set<Long> documentIds) {
        applicationEventPublisher.publishEvent(new MultipleItemsUpdatedEvent(this, documentIds));
    }

    public void publishDocumentUpdate(Long documentId) {
        applicationEventPublisher.publishEvent(new ItemUpdatedEvent(this, documentId));
    }

    public void publishDocumentUpdateContent(Long documentId) {
        applicationEventPublisher.publishEvent(new DocumentUpdateContent(this, documentId));
    }
}

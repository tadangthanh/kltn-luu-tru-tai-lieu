package vn.kltn.service.event.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import vn.kltn.service.event.DocumentUpdatedEvent;
import vn.kltn.service.event.MultipleDocumentsUpdatedEvent;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishDocumentsUpdate(Set<Long> documentIds) {
        applicationEventPublisher.publishEvent(new MultipleDocumentsUpdatedEvent(this, documentIds));
    }
    public void publishDocumentUpdate(Long documentId) {
        applicationEventPublisher.publishEvent(new DocumentUpdatedEvent(this, documentId));
    }
}

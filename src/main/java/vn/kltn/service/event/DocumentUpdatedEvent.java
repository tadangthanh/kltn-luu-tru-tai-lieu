package vn.kltn.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DocumentUpdatedEvent extends ApplicationEvent {
    private final Long documentId;

    public DocumentUpdatedEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }
}
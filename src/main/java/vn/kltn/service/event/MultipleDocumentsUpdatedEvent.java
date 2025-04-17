package vn.kltn.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;
@Getter
public class MultipleDocumentsUpdatedEvent extends ApplicationEvent {
    private final Set<Long> documentIds;

    public MultipleDocumentsUpdatedEvent(Object source,Set<Long> documentIds) {
        super(source);
        this.documentIds = documentIds;
    }
}

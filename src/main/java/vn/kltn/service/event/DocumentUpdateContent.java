package vn.kltn.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DocumentUpdateContent extends ApplicationEvent {
    private final Long documentId;

    public DocumentUpdateContent(Object source, Long documentId ){
        super(source);
        this.documentId = documentId;
    }
}

package vn.kltn.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ItemUpdatedEvent extends ApplicationEvent {
    private final Long itemId;

    public ItemUpdatedEvent(Object source, Long itemId) {
        super(source);
        this.itemId = itemId;
    }
}
package vn.kltn.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;
@Getter
public class MultipleItemsUpdatedEvent extends ApplicationEvent {
    private final Set<Long> itemIds;

    public MultipleItemsUpdatedEvent(Object source, Set<Long> itemIds) {
        super(source);
        this.itemIds = itemIds;
    }
}

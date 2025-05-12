package vn.kltn.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.kltn.service.IItemIndexService;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class ItemUpdatedEventListener {
    private final IItemIndexService itemIndexService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleItemUpdated(ItemUpdatedEvent event)  {
        Long docId = event.getItemId();
        itemIndexService.syncItem(docId);
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMultipleItemsUpdated(MultipleItemsUpdatedEvent event) {
        itemIndexService.syncItems(event.getItemIds());
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMultipleItemsUpdated(DocumentUpdateContent event) {
        itemIndexService.syncContentDocument(event.getDocumentId());
    }
}

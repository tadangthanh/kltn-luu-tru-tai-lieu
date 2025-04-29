package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Item;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.ItemRepo;

@Service
@Slf4j(topic = "ITEM_GETTER_SERVICE")
@RequiredArgsConstructor
public class ItemGetterService {
    private final ItemRepo itemRepo;


    public Item getItemByIdOrThrow(Long itemId) {
        return itemRepo.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Item with id {} not found", itemId);
                    return new ResourceNotFoundException("Không tìm thấy item với id: " + itemId);
                });
    }

}

package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.common.ItemType;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.SavedItem;
import vn.kltn.entity.User;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.ItemRepo;
import vn.kltn.repository.SavedItemRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IItemService;
import vn.kltn.service.ISavedItemService;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "SAVED_ITEM_SERVICE")
@RequiredArgsConstructor
public class SavedItemServiceImpl implements ISavedItemService {
    private final SavedItemRepo savedItemRepo;
    private final ItemRepo itemRepo;
    private final ItemMapper itemMapper;
    private final IItemService itemService;
    private final IAuthenticationService authenticationService;
    private final IDocumentService documentService;

    @Override
    public SavedItem addSavedItem(Long itemId) {
        log.info("Adding saved item with ID: {}", itemId);
        Item item = itemService.getItemByIdOrThrow(itemId);
        SavedItem savedItem = new SavedItem();
        savedItem.setItem(item);
        User user = authenticationService.getCurrentUser();
        savedItem.setUser(user);
        savedItemRepo.save(savedItem);
        return savedItem;
    }

    @Override
    public void removeSavedItem(Long savedItemId) {
        log.info("Removing saved item with ID: {}", savedItemId);
        savedItemRepo.deleteById(savedItemId);
    }
    private ItemResponse mapToItemResponse(Item item) {
        ItemResponse itemResponse = itemMapper.toResponse(item);
        if (item.getItemType().equals(ItemType.DOCUMENT)) {
            itemResponse.setSize(documentService.getItemByIdOrThrow(item.getId()).getCurrentVersion().getSize());
        }
        return itemResponse;
    }
    @Override
    public PageResponse<List<ItemResponse>> getSavedItems(Pageable pageable) {
        log.info("Getting saved items with page size: {}, page no: {}", pageable.getPageSize(), pageable.getPageNumber());
        Page<Item> savedItems = itemRepo.getPageItemSaved(pageable, authenticationService.getCurrentUser().getId());
        return PaginationUtils.convertToPageResponse(savedItems, pageable, this::mapToItemResponse);
    }
}

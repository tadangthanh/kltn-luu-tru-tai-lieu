package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.SavedItem;
import vn.kltn.entity.User;
import vn.kltn.exception.DuplicateResourceException;
import vn.kltn.repository.ItemRepo;
import vn.kltn.repository.SavedItemRepo;
import vn.kltn.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IItemService;
import vn.kltn.service.ISavedItemService;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "SAVED_ITEM_SERVICE")
@RequiredArgsConstructor
public class SavedItemServiceImpl implements ISavedItemService {
    private final SavedItemRepo savedItemRepo;
    private final IItemService itemService;
    private final ItemRepo itemRepo;
    private final IAuthenticationService authenticationService;
    private final ItemMapperService itemMapperService;

    @Override
    public SavedItem addSavedItem(Long itemId) {
        log.info("Adding saved item with ID: {}", itemId);
        // kiem tra su ton tai trong saved item, kiem tra item bi xoa chua
        if (savedItemRepo.existsByUserIdAndItemId(authenticationService.getCurrentUser().getId(), itemId)) {
            log.warn("Item with ID: {} is already saved by the user", itemId);
            throw new DuplicateResourceException("Item already saved");
        }
        Item item = itemService.getItemByIdOrThrow(itemId);
        SavedItem savedItem = new SavedItem();
        savedItem.setItem(item);
        User user = authenticationService.getCurrentUser();
        savedItem.setUser(user);
        savedItemRepo.save(savedItem);
        return savedItem;
    }

    @Override
    public void removeByItemId(Long itemId) {
        log.info("Removing saved item with itemId ID: {}", itemId);
        User user = authenticationService.getCurrentUser();
        savedItemRepo.deleteByItemIdAndUserId(itemId, user.getId());
    }


    @Override
    public PageResponse<List<ItemResponse>> getSavedItems(Pageable pageable) {
        log.info("Getting saved items with page size: {}, page no: {}", pageable.getPageSize(), pageable.getPageNumber());
        Page<Item> savedItems =
                itemRepo.getPageItemSaved(authenticationService.getCurrentUser().getId(), pageable);
        return PaginationUtils.convertToPageResponse(savedItems, pageable, itemMapperService::toResponse);
    }
}

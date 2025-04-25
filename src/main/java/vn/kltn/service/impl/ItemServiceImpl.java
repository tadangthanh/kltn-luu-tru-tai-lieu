package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.ItemRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.ItemSpecification;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IItemService;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "ITEM_SERVICE")
@RequiredArgsConstructor
public class ItemServiceImpl implements IItemService<Item, ItemResponse> {
    private final ItemRepo itemRepo;
    private final ItemMapper itemMapper;
    private final IAuthenticationService authenticationService;

    @Override
    public void deleteItemById(Long itemId) {

    }

    @Override
    public ItemResponse restoreItemById(Long itemId) {
        return null;
    }

    @Override
    public ItemResponse getItemById(Long itemId) {
        return null;
    }

    @Override
    public Item getItemByIdOrThrow(Long itemId) {
        return null;
    }

    @Override
    public void hardDeleteItemById(Long itemId) {

    }

    @Override
    public ItemResponse moveItemToFolder(Long itemId, Long folderId) {
        return null;
    }

    @Override
    public PageResponse<List<ItemResponse>> searchByCurrentUser(Pageable pageable, String[] items) {
        log.info("search items by current user page no: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        EntitySpecificationsBuilder<Item> builder = new EntitySpecificationsBuilder<>();
        User currentUser= authenticationService.getCurrentUser();
        Specification<Item> spec=Specification.where(ItemSpecification.notDeleted());
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), currentUser.getEmail()));
        spec = spec.or(ItemSpecification.hasPermissionForUser(currentUser.getId()));

        if (items != null && items.length > 0) {
            spec = spec.and(SpecificationUtil.buildSpecificationFromFilters(items, builder));
//            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
//            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), currentUser.getEmail()));
            Page<Item> pageAccessByResource = itemRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, itemMapper::toResponse);
        }
//        spec = (root, query, criteriaBuilder) -> root.get("deletedAt").isNull();
//        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), currentUser.getEmail()));
        return PaginationUtils.convertToPageResponse(itemRepo.findAll(spec, pageable), pageable, itemMapper::toResponse);
    }
}

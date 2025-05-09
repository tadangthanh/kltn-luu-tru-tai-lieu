package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.kltn.common.ItemType;
import vn.kltn.dto.request.ItemRequest;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.ItemRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.ItemSpecification;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "ITEM_SERVICE")
@RequiredArgsConstructor
public class ItemServiceImpl implements IItemService {
    private final ItemRepo itemRepo;
    private final ItemMapper itemMapper;
    private final IAuthenticationService authenticationService;
    private final ItemValidator itemValidator;
    private final IDocumentService documentService;
    private final IFolderService folderService;
    private final ItemMapperService itemMapperService;
    private final IPermissionService permissionService;

    @Override
    public PageResponse<List<ItemResponse>> getItemsByOwner(Pageable pageable, String[] items) {
        log.info("search items by current user page no: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        EntitySpecificationsBuilder<Item> builder = new EntitySpecificationsBuilder<>();
        User currentUser = authenticationService.getCurrentUser();
        // chung ta chi lay cac item khong bi xoa
        Specification<Item> spec = Specification.where(ItemSpecification.notDeleted());
        // la chu so huu
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"), currentUser.getId()));
        // hoac la nguoi duoc chia se
//        spec = spec.or(ItemSpecification.hasPermissionForUser(currentUser.getId()));
        if (items != null && items.length > 0) {
            boolean hasParentId = Arrays.stream(items)
                    .anyMatch(item -> item.startsWith("parent.id"));

            if (!hasParentId) {
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent")));
            }
            spec = spec.and(SpecificationUtil.buildSpecificationFromFilters(items, builder));
            Page<Item> pageAccessByResource = itemRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, itemMapperService::toResponse);
        } else {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent")));
        }

        return PaginationUtils.convertToPageResponse(itemRepo.findAll(spec, pageable), pageable, itemMapperService::toResponse);
    }

    @Override
    public PageResponse<List<ItemResponse>> getItemsSharedWithMe(Pageable pageable, String[] items) {
        log.info("search items shared with me by current user page no: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        EntitySpecificationsBuilder<Item> builder = new EntitySpecificationsBuilder<>();
        User currentUser = authenticationService.getCurrentUser();
        // chua bi xoa
        Specification<Item> spec = Specification.where(ItemSpecification.notDeleted());
        // duoc chia se
        spec = spec.and(ItemSpecification.hasPermissionForUser(currentUser.getId()));
        // không bị ẩn
        spec = spec.and(ItemSpecification.notHiddenShared());
        if (items != null && items.length > 0) {
            boolean hasParentId = Arrays.stream(items)
                    .anyMatch(item -> item.startsWith("parent.id"));

            if (!hasParentId) {
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent")));
            }
            spec = spec.and(SpecificationUtil.buildSpecificationFromFilters(items, builder));
            Page<Item> pageAccessByResource = itemRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, itemMapperService::toResponse);
        } else {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent")));
        }

        return PaginationUtils.convertToPageResponse(itemRepo.findAll(spec, pageable), pageable, itemMapperService::toResponse);
    }


    @Override
    public Item getItemByIdOrThrow(Long itemId) {
        return itemRepo.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Item not found for itemId: " + itemId));
    }

    @Override
    public void softDeleteItemById(Long itemId) {
        log.info("Soft delete item by id: {}", itemId);
        Item item = getItemByIdOrThrow(itemId);
        // resource chua bi xoa
        itemValidator.validateItemNotDeleted(item);
        // validate chu so huu hoac editor o resource cha
        if (item.getParent() != null) {
            itemValidator.validateCurrentUserIsOwnerOrEditorItem(item.getParent());
        }
        User currentUser = authenticationService.getCurrentUser();
        User owner = item.getOwner();
        // neu la chu so huu thi chuyen vao thung rac
        if (currentUser.getId().equals(owner.getId())) {
            if (item.getItemType().equals(ItemType.DOCUMENT)) {
                documentService.softDeleteDocumentById(item.getId());
            } else if (item.getItemType().equals(ItemType.FOLDER)) {
                folderService.softDeleteFolderById(item.getId());
            }
        } else {
            // nguoi thuc hien co quyen editor
//            permissionValidatorService.validatePermissionEditor(item, currentUser);
            // set parent = null la se dua resource nay vao drive cua toi
            item.setParent(null);
            permissionService.hidePermissionByItemIdAndUserId(item.getId(), currentUser.getId());
        }
    }

    @Override
    public PageResponse<List<String>> getEmailsSharedWithMe(Pageable pageable, String keyword) {
        log.info("get emails shared with me page no: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<String> emailsSharedWithMe;
        User currentUser = authenticationService.getCurrentUser();
        if (!StringUtils.hasText(keyword)) {
            emailsSharedWithMe = itemRepo.findOwnerEmailsSharedItemForMe(
                    currentUser.getId(), pageable
            );
        } else {
            emailsSharedWithMe = itemRepo.findOwnerEmailsSharedItemForMeFiltered(
                    currentUser.getId(), keyword, pageable
            );
        }

        return PageResponse.<List<String>>builder()
                .items(emailsSharedWithMe.getContent())
                .pageSize(pageable.getPageSize())
                .pageNo(pageable.getPageNumber())
                .totalPage(emailsSharedWithMe.getTotalPages())
                .totalItems(emailsSharedWithMe.getTotalElements())
                .hasNext(emailsSharedWithMe.hasNext())
                .build();
    }

    @Override
    public ItemResponse updateItem(Long itemId, ItemRequest itemRequest) {
        Item itemExist = itemRepo.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy item với id: " + itemId));
        itemMapper.updateItem(itemExist, itemRequest);
        itemRepo.save(itemExist);
        return itemMapperService.toResponse(itemExist);
    }

    @Override
    public PageResponse<List<ItemResponse>> getItemsMarkDelete(Pageable pageable) {
        log.info("Get items mark delete page no: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        User currentUser = authenticationService.getCurrentUser();
        Specification<Item> spec = Specification.where(ItemSpecification.markDeleted()).and(ItemSpecification.ownedBy(currentUser.getId())
                .and(ItemSpecification.nullParent()));
        return PaginationUtils.convertToPageResponse(itemRepo.findAll(spec, pageable), pageable, itemMapperService::toResponse);
    }

    @Override
    public void cleanUpTrash() {
        log.info("Clean up trash");
        User currentUser = authenticationService.getCurrentUser();
        Specification<Item> spec = Specification.where(ItemSpecification.markDeleted()).and(ItemSpecification.ownedBy(currentUser.getId()));
        List<Item> items = itemRepo.findAll(spec);
        for (Item item : items) {
            if (item.getItemType().equals(ItemType.FOLDER)) {
                folderService.hardDeleteFolderById(item.getId());
            } else if (item.getItemType().equals(ItemType.DOCUMENT)) {
                documentService.hardDeleteItemById(item.getId());
            }
        }
        log.info("Clean up trash done");
    }

    @Override
    public ItemResponse restoreItemById(Long itemId) {
        log.info("Restore item by id: {}", itemId);
        Item item = getItemByIdOrThrow(itemId);
        itemValidator.validateCurrentUserIsOwnerItem(item);
        if (item.getItemType().equals(ItemType.DOCUMENT)) {
            documentService.restoreItemById(item.getId());
        } else if (item.getItemType().equals(ItemType.FOLDER)) {
            folderService.restoreItemById(item.getId());
        }
        log.info("Restore item by id done: {}", itemId);
        return itemMapperService.toResponse(item);
    }

    @Override
    public void deleteItemForever(Long itemId) {
        log.info("Delete item forever by id: {}", itemId);
        Item item = getItemByIdOrThrow(itemId);
        itemValidator.validateCurrentUserIsOwnerItem(item);
        if (item.getItemType().equals(ItemType.DOCUMENT)) {
            documentService.hardDeleteItemById(item.getId());
        } else if (item.getItemType().equals(ItemType.FOLDER)) {
            folderService.hardDeleteFolderById(item.getId());
        }
        log.info("Delete item forever by id done: {}", itemId);
    }

}

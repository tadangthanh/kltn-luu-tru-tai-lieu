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
import vn.kltn.dto.BreadcrumbDto;
import vn.kltn.dto.request.ItemRequest;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.ItemRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.ItemSpecification;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;
import vn.kltn.util.PaginationItemUtils;
import vn.kltn.util.PaginationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final IItemIndexService itemIndexService;

    @Override
    public PageItemResponse<List<ItemResponse>> getMyItems(Pageable pageable, String[] filters) {
        log.info("get my items - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        User currentUser = authenticationService.getCurrentUser();
        Specification<Item> spec = buildBaseSpecification();
        // Chỉ lấy các item của người dùng hiện tại
        spec = spec.and(ItemSpecification.ownedBy(currentUser.getId()));

        List<BreadcrumbDto> breadcrumbDtos = new ArrayList<>();

        if (filters != null && filters.length > 0) {
            Long parentId = extractParentIdIfPresent(filters);

            if (parentId != null) {
                Item parentItem = itemRepo.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found for itemId: " + parentId));
                breadcrumbDtos.addAll(itemMapperService.buildBreadcrumb(parentItem));
            } else {
                spec = spec.and((root, query, cb) -> cb.isNull(root.get("parent")));
            }

            EntitySpecificationsBuilder<Item> builder = new EntitySpecificationsBuilder<>();
            spec = spec.and(SpecificationUtil.buildSpecificationFromFilters(filters, builder));
        } else {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("parent")));
        }

        Page<Item> itemPage = itemRepo.findAll(spec, pageable);
        return PaginationItemUtils.convertToPageItemResponse(itemPage, pageable, itemMapperService::toResponse, breadcrumbDtos);
    }

    private Specification<Item> buildBaseSpecification() {
        return Specification.where(ItemSpecification.notDeleted());

        // Nếu bạn muốn sau này mở lại quyền chia sẻ, có thể thêm vào đây:
        // .or(ItemSpecification.hasPermissionForUser(currentUser.getId()));
    }

    private Long extractParentIdIfPresent(String[] filters) {
        return Arrays.stream(filters)
                .filter(f -> f.startsWith("parent.id:"))
                .map(f -> {
                    try {
                        return Long.valueOf(f.split(":", 2)[1]);
                    } catch (NumberFormatException ex) {
                        throw new InvalidDataException("Invalid parent.id filter format");
                    }
                })
                .findFirst()
                .orElse(null);
    }


    @Override
    public PageItemResponse<List<ItemResponse>> getItemsSharedWithMe(Pageable pageable, String[] filters) {
        log.info("get item shared with me - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        User currentUser = authenticationService.getCurrentUser();
        Specification<Item> spec = buildBaseSpecification();
        // Chỉ lấy các item dc chia sẻ với người dùng hiện tại
        spec = spec.and(ItemSpecification.hasPermissionForUser(currentUser.getId()));
        // Chỉ lấy các item chưa bị ẩn
        spec = spec.and(ItemSpecification.hasPermissionItemNotHiddenForUser(currentUser.getId()));

        List<BreadcrumbDto> breadcrumbDtos = new ArrayList<>();

        if (filters != null && filters.length > 0) {
            Long parentId = extractParentIdIfPresent(filters);

            if (parentId != null) {
                Item parentItem = itemRepo.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found for itemId: " + parentId));
                breadcrumbDtos.addAll(itemMapperService.buildBreadcrumb(parentItem));
            } else {
                spec = spec.and((root, query, cb) -> cb.isNull(root.get("parent")));
            }

            EntitySpecificationsBuilder<Item> builder = new EntitySpecificationsBuilder<>();
            spec = spec.and(SpecificationUtil.buildSpecificationFromFilters(filters, builder));
        } else {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("parent")));
        }

        Page<Item> itemPage = itemRepo.findAll(spec, pageable);
        return PaginationItemUtils.convertToPageItemResponse(itemPage, pageable, itemMapperService::toResponse, breadcrumbDtos);
    }


    @Override
    public Item getItemByIdOrThrow(Long itemId) {
        return itemRepo.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Item not found for itemId: " + itemId));
    }

//    @Override
//    public void softDeleteItemById(Long itemId) {
//        log.info("Soft delete item by id: {}", itemId);
//        Item item = getItemByIdOrThrow(itemId);
//        // resource chua bi xoa
//        itemValidator.validateItemNotDeleted(item);
//        // validate chu so huu hoac editor o resource cha
//        if (item.getParent() != null) {
//            itemValidator.validateCurrentUserIsOwnerOrEditorItem(item.getParent());
//        }
//        User currentUser = authenticationService.getCurrentUser();
//        User owner = item.getOwner();
//        // neu la chu so huu thi chuyen vao thung rac
//        if (currentUser.getId().equals(owner.getId())) {
//            if (item.getItemType().equals(ItemType.DOCUMENT)) {
//                documentService.softDeleteDocumentById(item.getId());
//            } else if (item.getItemType().equals(ItemType.FOLDER)) {
//                folderService.softDeleteFolderById(item.getId());
//            }
//        } else {
//            // nguoi thuc hien co quyen editor

    /// /            permissionValidatorService.validatePermissionEditor(item, currentUser);
//            // set parent = null la se dua resource nay vao drive cua toi
//            item.setParent(null);
//            permissionService.hidePermissionByItemIdAndUserId(item.getId(), currentUser.getId());
//        }
//    }
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
            List<Long> itemIds = itemRepo.findAllItemIdsRecursively(item.getId());
            itemRepo.updateDeletedAtAndPermanentDeleteAt(itemIds, LocalDateTime.now(), LocalDateTime.now().plusDays(30));
            itemIndexService.markDeleteItems(itemIds, true);
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
                .and(ItemSpecification.nullParent().or(ItemSpecification.parentNotMarkDeleted())));
        return PaginationUtils.convertToPageResponse(itemRepo.findAll(spec, pageable), pageable, itemMapperService::toResponse);
    }

    @Override
    public void cleanUpTrash() {
        log.info("Clean up trash");
        User currentUser = authenticationService.getCurrentUser();
        Specification<Item> spec = Specification.where(ItemSpecification.markDeleted()).and(ItemSpecification.ownedBy(currentUser.getId())).and(ItemSpecification.nullParent().or(ItemSpecification.parentNotMarkDeleted()));
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
//        if (item.getItemType().equals(ItemType.DOCUMENT)) {
//            documentService.restoreItemById(item.getId());
//        } else if (item.getItemType().equals(ItemType.FOLDER)) {
//            folderService.restoreItemById(item.getId());
//        }
        // Chuyển item về trạng thái chưa xóa
        List<Long> itemIds = itemRepo.findAllItemIdsRecursively(item.getId());
        itemRepo.updateDeletedAtAndPermanentDeleteAt(itemIds, null, null);
        itemIndexService.markDeleteItems(itemIds, false);
        log.info("Restore item by id done: {}", itemId);
        return itemMapperService.toResponse(item);
    }

    @Override
    public void deleteItemForever(Long itemId) {
        log.info("Delete item forever by id: {}", itemId);
        Item item = getItemByIdOrThrow(itemId);
        itemValidator.validateCurrentUserIsOwnerItem(item);
//        if (item.getItemType().equals(ItemType.DOCUMENT)) {
//            documentService.hardDeleteItemById(item.getId());
//        } else if (item.getItemType().equals(ItemType.FOLDER)) {
//            folderService.hardDeleteFolderById(item.getId());
//        }
        // Xóa item vĩnh viễn
        List<Long> itemIds = itemRepo.findAllItemIdsRecursively(item.getId());
        itemRepo.deleteAllById(itemIds);
        itemIndexService.deleteIndexByIdList(itemIds);
        log.info("Delete item forever by id done: {}", itemId);
    }

    @Override
    public void showItem(Long itemId) {
        Item item = getItemByIdOrThrow(itemId);
        permissionService.showItem(itemId);
    }

}

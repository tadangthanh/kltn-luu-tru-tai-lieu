package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.kltn.common.ItemType;
import vn.kltn.dto.BreadcrumbDto;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.repository.SavedItemRepo;
import vn.kltn.service.IAuthenticationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "ITEM_MAPPER_SERVICE")
public class ItemMapperService {
    private final ItemMapper itemMapper;
    private final SavedItemRepo savedItemRepo;
    private final DocumentRepo documentRepo;
    private final IAuthenticationService authenticationService;
    private final PermissionRepo permissionRepo;

    public ItemResponse toResponse(Item item) {
        ItemResponse itemResponse = itemMapper.toResponse(item);
        if (item.getItemType().equals(ItemType.DOCUMENT)) {
            itemResponse.setSize(documentRepo.findById(item.getId()).orElseThrow(() -> {
                log.error("Document not found for itemId: {}", item.getId());
                return new RuntimeException("Document not found");
            }).getCurrentVersion().getSize());
        }
        User user = authenticationService.getCurrentUser();
        itemResponse.setSaved(savedItemRepo.existsByUserIdAndItemId(user.getId(), item.getId()));
        itemResponse.setSharedWithMe(permissionRepo.existsByRecipientIdAndItemId(user.getId(), item.getId()));
        return itemResponse;
    }

    public List<ItemResponse> toResponse(List<Item> items) {
        return items.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public List<BreadcrumbDto> buildBreadcrumb(Item item) {
        List<BreadcrumbDto> breadcrumb = new ArrayList<>();
        Item current = item;

        while (current.getParent() != null) {
            Item itemFolder = current.getParent(); // parent là Folder, kế thừa Item
            breadcrumb.add(new BreadcrumbDto(itemFolder.getId(), itemFolder.getName()));
            current = itemFolder;
        }

        // Đảo ngược vì hiện tại đang từ dưới lên
        Collections.reverse(breadcrumb);
        // Thêm item hiện tại vào breadcrumb
        breadcrumb.add(new BreadcrumbDto(item.getId(), item.getName()));
        return breadcrumb;
    }

}

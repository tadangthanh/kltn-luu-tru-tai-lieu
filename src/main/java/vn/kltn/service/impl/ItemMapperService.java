package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.kltn.common.ItemType;
import vn.kltn.dto.BreadcrumbDto;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.SavedItemRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemMapperService {
    private final ItemMapper itemMapper;
    private final IDocumentService documentService;
    private final SavedItemRepo savedItemRepo;
    private final IAuthenticationService authenticationService;

    public ItemResponse toResponse(Item item) {
        ItemResponse itemResponse = itemMapper.toResponse(item);
        if (item.getItemType().equals(ItemType.DOCUMENT)) {
            itemResponse.setSize(documentService.getItemByIdOrThrow(item.getId()).getCurrentVersion().getSize());
        }
        User user = authenticationService.getCurrentUser();
        if (user != null) {
            itemResponse.setSaved(savedItemRepo.existsByUserIdAndItemId(user.getId(), item.getId()));
        }
        return itemResponse;
    }

    public List<BreadcrumbDto> buildBreadcrumb(Item item) {
        List<BreadcrumbDto> breadcrumb = new ArrayList<>();
        Item current = item;

        while (current.getParent() != null) {
            Folder parent = current.getParent(); // parent là Folder, kế thừa Item
            breadcrumb.add(new BreadcrumbDto(parent.getId(), parent.getName()));
            current = parent;
        }

        // Đảo ngược vì hiện tại đang từ dưới lên
        Collections.reverse(breadcrumb);
        // Thêm item hiện tại vào breadcrumb
        breadcrumb.add(new BreadcrumbDto(item.getId(), item.getName()));
        return breadcrumb;
    }

}

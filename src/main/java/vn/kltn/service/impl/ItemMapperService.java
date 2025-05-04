package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.kltn.common.ItemType;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.SavedItemRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;

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
}

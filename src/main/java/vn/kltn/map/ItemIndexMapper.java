package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.kltn.entity.Item;
import vn.kltn.index.ItemIndex;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ItemIndexMapper {
    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "id", qualifiedByName = "longToString")
    ItemIndex toIndex(Item item);

    @Named("longToString")
    static String longToString(Long id) {
        return id != null ? id.toString() : null;
    }
}

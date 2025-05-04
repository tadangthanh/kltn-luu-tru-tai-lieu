package vn.kltn.map;

import org.mapstruct.Mapper;
import vn.kltn.dto.response.SavedItemResponse;
import vn.kltn.entity.SavedItem;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface SavedItemMapper {
    SavedItemResponse toResponse(SavedItem savedItem);
}

package vn.kltn.map;

import org.mapstruct.Mapper;
import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.Tag;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface TagMapper {
    Tag requestToEntity(TagRequest request);

}

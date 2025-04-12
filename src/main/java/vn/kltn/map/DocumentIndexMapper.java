package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.kltn.entity.Document;
import vn.kltn.index.DocumentIndex;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentIndexMapper {
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "id", qualifiedByName = "longToString")
    DocumentIndex toIndex(Document document);

    @Named("longToString")
    static String longToString(Long id) {
        return id != null ? id.toString() : null;
    }
}

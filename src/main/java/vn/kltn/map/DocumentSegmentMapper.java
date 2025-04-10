package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.entity.Document;
import vn.kltn.index.DocumentSegmentEntity;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentSegmentMapper {
    @Mapping(target = "documentId", source = "id")
    DocumentSegmentEntity toSegmentEntity(Document document);
}

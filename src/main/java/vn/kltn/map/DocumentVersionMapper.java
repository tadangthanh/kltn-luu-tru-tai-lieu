package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.DocumentVersionResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentVersion;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentVersionMapper {

    @Mapping(target = "documentId", source = "document.id")
    DocumentVersionResponse toDocumentVersionResponse(DocumentVersion documentVersion);

    @Mapping(target = "document", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentVersion toDocumentVersion(Document document);
}

package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.DocumentVersionResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentVersion;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentVersionMapper {

    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "type", source = "document.type")
    DocumentVersionResponse toDocumentVersionResponse(DocumentVersion documentVersion);

    @Mapping(target = "document", ignore = true)
    @Mapping(target = "id", ignore = true)
    DocumentVersion toDocumentVersion(Document document);

    List<DocumentVersionResponse> toDocumentVersionResponse(List<DocumentVersion> documentVersions);
}

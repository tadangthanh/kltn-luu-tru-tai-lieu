package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentMapper {
    Document toDocument(DocumentRequest documentRequest);

    @Mapping(target = "ownerName",source = "owner.fullName")
    @Mapping(target = "ownerEmail",source = "owner.email")
    @Mapping(target = "folderId",source = "folder.id")
    DocumentResponse toDocumentResponse(Document document);

    @Mapping(target = "id", ignore = true)
    Document copyDocument(Document document);
}

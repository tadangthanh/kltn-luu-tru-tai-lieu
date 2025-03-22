package vn.kltn.map;

import org.mapstruct.Mapper;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentMapper {
    Document toDocument(DocumentRequest documentRequest);

    DocumentResponse toDocumentResponse(Document document);
}

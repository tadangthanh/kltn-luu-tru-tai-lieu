package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentMapper {
    Document toDocument(DocumentRequest documentRequest);

    @Mapping(target = "ownerName", source = "owner.fullName")
    @Mapping(target = "ownerEmail", source = "owner.email")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "version", source = "currentVersion.version")
    @Mapping(target = "size", source = "currentVersion.size")
    DocumentResponse toDocumentResponse(Document document);

    List<DocumentResponse> toDocumentResponseList(List<Document> documents);

    @Mapping(target = "id", ignore = true)
    Document copyDocument(Document document);

    @Mapping(target = "name", source = "fileName")
    @Mapping(target = "type", source = "contentType")
    Document mapFileBufferToDocument(FileBuffer fileBuffer);

    void updateDocument(@MappingTarget Document document, DocumentRequest documentRequest);

}

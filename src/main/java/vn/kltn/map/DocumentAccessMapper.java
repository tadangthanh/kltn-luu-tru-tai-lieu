package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.DocumentAccessResponse;
import vn.kltn.entity.DocumentAccess;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentAccessMapper {
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "owner", source = "document.owner")
    DocumentAccessResponse toDocumentAccessResponse(DocumentAccess documentAccess);

}

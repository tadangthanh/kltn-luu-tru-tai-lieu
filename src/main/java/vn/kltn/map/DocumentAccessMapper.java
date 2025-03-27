package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.DocumentAccessResponse;
import vn.kltn.entity.DocumentAccess;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DocumentMapper.class}, nullValuePropertyMappingStrategy = IGNORE)
public interface DocumentAccessMapper {
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
//    @Mapping(target = "owner", source = "resource.owner")
//    @Mapping(target = "resource", source = "resource")
    DocumentAccessResponse toDocumentAccessResponse(DocumentAccess documentAccess);

}

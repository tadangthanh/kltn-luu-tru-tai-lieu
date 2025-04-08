package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.PreviewImageResponse;
import vn.kltn.entity.PreviewImage;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface PreviewImageMapper {
    @Mapping(source = "document.id", target = "documentId")
    PreviewImageResponse toPreviewImageResponse(PreviewImage previewImage);
}

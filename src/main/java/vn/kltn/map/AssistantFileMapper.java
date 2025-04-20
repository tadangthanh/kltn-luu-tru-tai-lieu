package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.AssistantFileDto;
import vn.kltn.entity.AssistantFile;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface AssistantFileMapper {
    @Mapping(target = "assistantFileId", source = "assistantFile.id")
    AssistantFileDto toDto(AssistantFile assistantFile);

    AssistantFile toEntity(AssistantFileDto assistantFileDto);
}

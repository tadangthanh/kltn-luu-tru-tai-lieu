package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.AssistantFileDto;
import vn.kltn.entity.AssistantFile;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface AssistantFileMapper {
    @Mapping(target = "userId", source = "user.id")
    AssistantFileDto toDto(AssistantFile assistantFile);

    AssistantFile toEntity(AssistantFileDto assistantFileDto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget AssistantFile assistantFile,
                      AssistantFileDto assistantFileDto);
}

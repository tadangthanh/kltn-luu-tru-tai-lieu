package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.request.AssistantFileRequest;
import vn.kltn.dto.response.AssistantFileDto;
import vn.kltn.entity.AssistantFile;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface AssistantFileMapper {
    AssistantFileDto toResponse(AssistantFile assistantFile);

    List<AssistantFileDto> toResponse(List<AssistantFile> assistantFiles);

    @Mapping(target = "id", ignore = true)
    AssistantFile toEntity(AssistantFileRequest assistantFileRequest);

    @Mapping(target = "id", ignore = true)
    List<AssistantFile> toEntity(List<AssistantFileRequest> assistantFilesRequest);


    @Mapping(target = "id", ignore = true)
    List<AssistantFile> listToEntity(List<AssistantFileDto> assistantFileDtoList);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget AssistantFile assistantFile,
                      AssistantFileRequest assistantFileRequest);
}

package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.FileStatisticResponse;
import vn.kltn.entity.FileStatistic;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileStatisticMapper {
    @Mapping(target = "fileId", source = "file.id")
    FileStatisticResponse toFileStatisticResponse(FileStatistic fileStatistic);
}

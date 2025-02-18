package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface RepoMapper {
    RepoMapper INSTANCE = Mappers.getMapper(RepoMapper.class);
    RepoResponseDto entityToResponse(Repo repo);

    Repo requestToEntity(RepoRequestDto repoRequestDto);
}

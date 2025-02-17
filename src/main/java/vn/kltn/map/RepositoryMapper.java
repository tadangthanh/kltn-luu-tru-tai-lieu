package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repository;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface RepositoryMapper {
    @Mapping(target = "name", source = "name")
    RepoResponseDto entityToResponse(Repository repository);

    Repository requestToEntity(RepositoryRequestDto repositoryRequestDto);
}

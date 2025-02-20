package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface RepoMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerEmail", source = "owner.email")
    @Mapping(target = "ownerName", source = "owner.fullName")
    RepoResponseDto entityToResponse(Repo repo);

    Repo requestToEntity(RepoRequestDto repoRequestDto);
}

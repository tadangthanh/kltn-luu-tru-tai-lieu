package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.RepoActivityResponse;
import vn.kltn.entity.RepoActivity;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface RepoActivityMapper {
    @Mapping(target = "repoName", source = "repo.name")
    @Mapping(target = "repoId", source = "repo.id")
    @Mapping(target = "authorName", source = "user.fullName")
    @Mapping(target = "authorEmail", source = "user.email")
    @Mapping(target = "authorId", source = "user.id")
    RepoActivityResponse toResponse(RepoActivity entity);
}

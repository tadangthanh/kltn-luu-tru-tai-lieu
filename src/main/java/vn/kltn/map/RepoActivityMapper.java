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
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberEmail", source = "member.user.email")
    @Mapping(target = "memberName", source = "member.user.fullName")
    RepoActivityResponse toResponse(RepoActivity entity);
}

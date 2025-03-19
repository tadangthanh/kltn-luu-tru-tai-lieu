package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.MemberResponse;
import vn.kltn.entity.Member;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface RepoMemberMapper {
    @Mapping(target = "memberName", source = "user.fullName")
    @Mapping(target = "memberEmail", source = "user.email")
    @Mapping(target = "repoId", source = "repo.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "role",source = "role.name")
    @Mapping(target = "roleDescription",source = "role.description")
    MemberResponse toRepoMemberInfoResponse(Member member);

}

package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.entity.RepoMember;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface RepoMemberMapper {
    @Mapping(target = "memberName", source = "user.fullName")
    @Mapping(target = "memberEmail", source = "user.email")
    RepoMemberInfoResponse toRepoMemberInfoResponse(RepoMember repoMember);

}

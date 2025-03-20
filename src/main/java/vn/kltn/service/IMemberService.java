package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.RoleName;
import vn.kltn.dto.response.MemberResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Member;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;

import java.util.List;

public interface IMemberService {

    MemberResponse sendInvitationRepo(Long repoId, Long userId, Long roleId);

    Member getAuthMemberWithRepoId(Long repoId);

    Member getMemberByRepoIdAndUserId(Long repoId, Long userId);

    boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    void saveMemberWithRoleAdmin(Repo repo, User user);

    int countMemberByRepoId(Long repoId);

    void deleteMemberByRepoIdAndUserId(Long repoId, Long userId);

    String getSasTokenByAuthMemberWithRepo(Repo repo);

    MemberResponse disableMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse enableMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse leaveRepo(Long repoId);

    void removeMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse updateMemberRoleByRepoIdAndUserId(Long repoId, Long userId, Long roleId);

    PageResponse<List<MemberResponse>> getListMemberByRepoId(Long repoId, Pageable pageable);

    boolean userHasAnyRoleRepoId(Long repoId, Long id, RoleName[] listRole);
}


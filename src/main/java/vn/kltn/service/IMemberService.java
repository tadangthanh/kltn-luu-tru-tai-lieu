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

    Member getMemberById(Long memberId);

    Member getMemberByEmailWithRepoId(Long repoId, String email);

    Member getMemberByRepoIdAndUserId(Long repoId, Long userId);

    boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    void saveMemberWithRoleAdmin(Repo repo, User user);

    int countMemberByRepoId(Long repoId);

    void deleteMemberByRepoIdAndUserId(Long repoId, Long userId);

    String getSasTokenByAuthMemberWithRepo(Repo repo);

    MemberResponse disableMemberById(Long memberId);

    MemberResponse enableMemberById(Long memberId);

    MemberResponse leaveRepo(Long repoId);

    MemberResponse removeMemberById(Long memberId);

    MemberResponse updateMemberRoleById(Long memberId, Long roleId);

    PageResponse<List<MemberResponse>> getListMemberByRepoId(Long repoId, Pageable pageable);

    boolean userHasAnyRoleRepoId(Long repoId, Long id, RoleName[] listRole);
}


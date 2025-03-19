package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.MemberStatus;
import vn.kltn.dto.response.MemberResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Member;
import vn.kltn.entity.MemberRole;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;

import java.util.List;

public interface IMemberService {

    Member getAuthMemberWithRepoId(Long repoId);

    Member updateSasTokenMember(Repo repo, Member member);

    Member getMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    Member getMemberByRepoIdAndUserId(Long repoId, Long userId);

    Member getMemberWithStatus(Long repoId, Long userId, MemberStatus status);

    boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    void saveMemberWithRoleId(Repo repo, User user, Long roleId);

    void saveMemberWithRoleAdmin(Repo repo, User user);

    int countMemberByRepoId(Long repoId);

    Member updateMemberPermissions(Member member, MemberRole role, String containerName);

    void deleteMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse toRepoMemberInfoResponse(Member member);

    String getSasTokenByAuthMemberWithRepo(Repo repo);

    MemberResponse disableMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse enableMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse leaveRepo(Long repoId);

    void removeMemberByRepoIdAndUserId(Long repoId, Long userId);

    MemberResponse updateMemberRoleByRepoIdAndUserId(Long repoId, Long userId, Long roleId);

    PageResponse<List<MemberResponse>> getListMemberByRepoId(Long repoId, Pageable pageable);
}


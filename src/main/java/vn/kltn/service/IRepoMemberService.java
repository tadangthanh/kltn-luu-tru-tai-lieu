package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;
import vn.kltn.entity.User;

import java.util.List;
import java.util.Set;

public interface IRepoMemberService {

    RepoMember getAuthMemberWithRepoId(Long repoId);

    RepoMember getMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    RepoMember getMemberByRepoIdAndUserId(Long repoId, Long userId);

    PageResponse<List<RepoMemberInfoResponse>> getListMemberByRepoId(Long repoId, Pageable pageable);

    boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    RepoMember saveMemberRepoWithPermission(Repo repo, User user, Set<RepoPermission> permissions);

    int countMemberActiveByRepoId(Long repoId);

    int countMemberByRepoId(Long repoId);

    RepoMember updateMemberPermissions(RepoMember repoMember, Set<RepoPermission> requestedPermissions, String containerName);

    void deleteMemberByRepoIdAndUserId(Long repoId, Long userId);


    RepoMember updateSasTokenByRepoIdAndUserId(Long repoId, Long userId);

}


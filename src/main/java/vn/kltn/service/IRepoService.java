package vn.kltn.service;

import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;

import java.util.Set;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest);

    void removeMemberFromRepository(Long repoId, Long memberId);

    RepoResponseDto updatePermissionMember(Long repoId, Long memberId, Set<RepoPermission> requestedPermissions);

    RepoResponseDto acceptInvitation(Long repoId, String token);

    RepoResponseDto rejectInvitation(Long repoId, String email);

    Set<RepoMemberInfoResponse> getListMember(Long repoId);

    RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto);

    Set<RepoPermission> getPermissionMemberAuthByRepoId(Long repoId);

    RepoMember getRepoMemberByUserIdAndRepoId(Long userId, Long repoId);

    Repo getRepositoryById(Long id);

    RepoMember getRepoMemberById(Long repoMemberId);

    boolean hasPermission(Long repoId, Long userId,RepoPermission permission);

}

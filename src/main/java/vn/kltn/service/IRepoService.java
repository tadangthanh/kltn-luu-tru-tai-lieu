package vn.kltn.service;

import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;

import java.util.Set;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest);

    void removeMemberByRepoIdAndUserId(Long repoId, Long userId);

    RepoResponseDto updatePermissionMemberByRepoIdAndUserId(Long repoId, Long userId, Set<RepoPermission> requestedPermissions);

    RepoResponseDto acceptInvitation(Long repoId, String token);

    RepoResponseDto rejectInvitation(Long repoId, String email);

    RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto);

    Repo getRepositoryById(Long id);

    boolean hasPermission(Long repoId, Long userId, RepoPermission permission);

}

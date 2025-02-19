package vn.kltn.service;

import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;

import java.util.Set;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(Long repoId,Long userId, Set<RepoPermission> permissionRequest);

    void removeMemberFromRepository(Long repoId,Long memberId);

    RepoResponseDto updatePermissionForMember(Long repoId, Long memberId, Set<RepoPermission> permissionRequest);
}

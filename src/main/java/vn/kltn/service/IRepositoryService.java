package vn.kltn.service;

import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.PermissionRepoDto;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepoResponseDto;

import java.util.List;

public interface IRepositoryService {
    RepoResponseDto createRepository(RepositoryRequestDto repositoryRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(Long repositoryId, Long userId, List<RepoPermission> permissions);

}

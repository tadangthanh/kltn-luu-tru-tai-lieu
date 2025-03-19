package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.RoleName;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;

import java.util.List;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(Long repoId, Long userId, Long roleId);

    RepoResponseDto acceptInvitation(Long repoId, String token);

    RepoResponseDto rejectInvitation(Long repoId, String email);

    RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto);

    Repo getRepositoryById(Long id);

    boolean userHasAnyRoleRepoId(Long repoId, Long userId, RoleName[] listRole);


    PageResponse<List<RepoResponseDto>> getPageResponseByUserAuth(Pageable pageable);

}

package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;

import java.util.List;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto acceptInvitation(Long repoId, String token);

    RepoResponseDto rejectInvitation(Long repoId, String email);

    RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto);

    Repo getRepositoryById(Long repoId);

    PageResponse<List<RepoResponseDto>> getPageRepoResponseByUserAuth(Pageable pageable);

}

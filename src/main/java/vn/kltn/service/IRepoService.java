package vn.kltn.service;

import vn.kltn.dto.request.RepoMemberRequest;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(RepoMemberRequest repoMemberRequest);

    void removeMemberFromRepository(Long repoId,Long memberId);

    RepoResponseDto updatePermissionForMember(RepoMemberRequest repoMemberRequest);
}

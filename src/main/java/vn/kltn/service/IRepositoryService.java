package vn.kltn.service;

import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepositoryResponseDto;

public interface IRepositoryService {
    RepositoryResponseDto createRepository(RepositoryRequestDto repositoryRequestDto);
}

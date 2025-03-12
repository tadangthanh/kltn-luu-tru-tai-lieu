package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Repo;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.RepositoryRepo;

@RequiredArgsConstructor
@Slf4j(topic = "REPO_COMMON_SERVICE")
@Service
public class RepoCommonService {
    private final RepositoryRepo repositoryRepo;

    public Repo getRepositoryById(Long id) {
        return repositoryRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy repository, id: {}", id);
            return new ResourceNotFoundException("Repository not found");
        });
    }
}

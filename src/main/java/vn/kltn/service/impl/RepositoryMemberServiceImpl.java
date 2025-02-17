package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.repository.RepositoryMemberPermissionRepo;
import vn.kltn.repository.RepositoryMemberRepo;
import vn.kltn.service.IRepositoryMemberService;

@Service
@Slf4j(topic = "REPOSITORY_MEMBER_SERVICE")
@RequiredArgsConstructor
@Transactional
public class RepositoryMemberServiceImpl implements IRepositoryMemberService {
    private final RepositoryMemberRepo repositoryMemberRepo;
    private final RepositoryMemberPermissionRepo repositoryMemberPermissionRepo;

    @Override
    public void deleteMember(Long repositoryId, Long memberId) {
        log.info("Delete member id: {} from repository id: {}", memberId, repositoryId);
        repositoryMemberPermissionRepo.deleteByMemberId(memberId);
        repositoryMemberRepo.deleteByRepositoryIdAndUserId(repositoryId, memberId);
    }

    @Override
    public void deleteMemberByRepositoryId(Long repositoryId) {
        log.info("Delete all member from repository id: {}", repositoryId);

    }
}

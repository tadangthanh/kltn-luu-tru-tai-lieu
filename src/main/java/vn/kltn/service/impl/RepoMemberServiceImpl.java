package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.RepoMemberMapper;
import vn.kltn.repository.RepoMemberRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IRepoMemberService;

import java.util.Set;

@Service
@Transactional
@Slf4j(topic = "REPO_MEMBER_SERVICE")
@RequiredArgsConstructor
public class RepoMemberServiceImpl implements IRepoMemberService {
    private final RepoMemberRepo repoMemberRepo;
    private final IAzureStorageService azureStorageService;
    private final IAuthenticationService authenticationService;
    private final RepoCommonService repoCommonService;
    private final RepoMemberMapper repoMemberMapper;

    @Override
    public RepoMember getAuthMemberWithRepoId(Long repoId) {
        User authUser = authenticationService.getAuthUser();
        return repoMemberRepo.getMemberActiveByRepoIdAndUserId(repoId, authUser.getId()).orElseThrow(() -> {
            log.error("{} không phải chủ sở hữu repo: {}", authUser.getEmail(), repoId);
            return new ResourceNotFoundException("Bạn không phải chủ sở hữu repo");
        });
    }

    @Override
    public RepoMember saveMember(RepoMember repoMember) {
        return repoMemberRepo.save(repoMember);
    }

    @Override
    public RepoMember updateSasTokenMember(Repo repo, RepoMember repoMember) {
        String newSasToken = azureStorageService.generatePermissionRepo(repo.getContainerName(), repoMember.getPermissions());
        repoMember.setSasToken(newSasToken);
        return repoMemberRepo.save(repoMember);
    }

    @Override
    public RepoMember getMemberActiveByRepoIdAndUserId(Long repoId, Long userId) {
        return repoMemberRepo.findRepoMemberActiveByRepoIdAndUserId(repoId, userId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên active, userId: {}, trong repo repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên: " + userId + " trong repo: " + repoId);
        });
    }

    @Override
    public RepoMember getMemberByRepoIdAndUserId(Long repoId, Long userId) {
        return repoMemberRepo.findRepoMemberByRepoIdAndUserId(repoId, userId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, userId: {}, trong repo repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public RepoMember getMemberWithStatus(Long repoId, Long userId, MemberStatus status) {
        return repoMemberRepo.findRepoMemberByRepoIdAndUserIdAndStatus(repoId, userId, status).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên {}, userId: {}, trong repo repoId: {}", status, userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public Page<RepoMember> getPageMember(Long repoId, Pageable pageable) {
        return repoMemberRepo.findAllByRepoId(repoId, pageable);
    }


    @Override
    public boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId) {
        return repoMemberRepo.isExistMemberActiveByRepoIdAndUserId(repoId, userId);
    }

    @Override
    public RepoMember saveMemberRepoWithPermission(Repo repo, User user, Set<RepoPermission> permissions) {
        RepoMember repoMember = mapToRepoMember(repo, user, permissions);
        return repoMemberRepo.save(repoMember);
    }

    private RepoMember mapToRepoMember(Repo repo, User user, Set<RepoPermission> permissions) {
        RepoMember repoMember = new RepoMember();
        repoMember.setUser(user);
        repoMember.setRepo(repo);
        repoMember.setPermissions(permissions);
        MemberStatus status = getMemberStatus(repo, user);
        repoMember.setStatus(status);
        if (status.equals(MemberStatus.ACTIVE)) {
            repoMember.setSasToken(getSasToken(repo, permissions));
        }
        return repoMember;
    }

    @Override
    public int countMemberActiveByRepoId(Long repoId) {
        return repoMemberRepo.countMemberActiveByRepoId(repoId);
    }

    @Override
    public int countMemberByRepoId(Long repoId) {
        return repoMemberRepo.countRepoMemberByRepoId(repoId);
    }

    @Override
    public RepoMember updateMemberPermissions(RepoMember repoMember, Set<RepoPermission> requestedPermissions, String containerName) {
        String sasToken = generateSasTokenForMember(containerName, requestedPermissions);
        repoMember.setPermissions(requestedPermissions);
        repoMember.setSasToken(sasToken);
        return repoMemberRepo.save(repoMember);
    }

    @Override
    public void deleteMemberByRepoIdAndUserId(Long repoId, Long userId) {
        RepoMember repoMember = getMemberByRepoIdAndUserId(repoId, userId);
        repoMemberRepo.delete(repoMember);
    }

    @Override
    public RepoMemberInfoResponse toRepoMemberInfoResponse(RepoMember repoMember) {
        return repoMemberMapper.toRepoMemberInfoResponse(repoMember);
    }

    @Override
    public RepoMember updateSasTokenByRepoIdAndUserId(Long repoId, Long userId) {
        RepoMember repoMember = getMemberByRepoIdAndUserId(repoId, userId);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        String newSasToken = azureStorageService.generatePermissionRepo(repo.getContainerName(), repoMember.getPermissions());
        repoMember.setSasToken(newSasToken);
        return repoMemberRepo.save(repoMember);
    }

    private String generateSasTokenForMember(String containerName, Set<RepoPermission> permissions) {
        return azureStorageService.generatePermissionRepo(containerName, permissions);
    }

    private String getSasToken(Repo repo, Set<RepoPermission> permissions) {
        return azureStorageService.generatePermissionRepo(repo.getContainerName(), permissions);
    }

    private boolean isOwnerRepo(Repo repo, User user) {
        return repo.getOwner().getId().equals(user.getId());
    }

    private MemberStatus getMemberStatus(Repo repo, User user) {
        // neu member la chu so huu thi luon la active
        if (isOwnerRepo(repo, user)) {
            return MemberStatus.ACTIVE;
        }
        return MemberStatus.INVITED;
    }
}

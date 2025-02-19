package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UnauthorizedException;
import vn.kltn.map.RepoMapper;
import vn.kltn.repository.RepoMemberRepo;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IRepoService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j(topic = "REPOSITORY_SERVICE")
@RequiredArgsConstructor
@Transactional
public class RepoServiceImpl implements IRepoService {
    private final IAzureStorageService azureStorageService;
    private final RepoMapper repoMapper;
    private final RepositoryRepo repositoryRepo;
    private final UserRepo userRepo;
    private final RepoMemberRepo repoMemberRepo;

    @Override
    public RepoResponseDto createRepository(RepoRequestDto repoRequestDto) {
        // tao container tuong ung voi repository
        Repo repo = saveRepoAndCreateContainer(repoRequestDto);
        return convertRepositoryToResponse(repo);
    }

    private Repo saveRepoAndCreateContainer(RepoRequestDto repoRequestDto) {
        String uuid = UUID.randomUUID().toString();
        String containerName = repoRequestDto.getName() + "-" + uuid;
        // tao container truoc khi tao repository
        azureStorageService.createContainerForRepository(containerName);
        //create repository
        User owner = getAuthUser();
        Repo repo = repoMapper.requestToEntity(repoRequestDto);
        repo.setOwner(owner);
        repo.setContainerName(containerName);
        repo = repositoryRepo.save(repo);
        return repo;
    }

    @Override
    public void deleteRepository(Long id) {
        Repo repo = getRepositoryByIdOrThrow(id);
        User authUser = getAuthUser();
        if (!isOwner(repo, authUser)) {
            log.error("Không có quyền xóa repository, id: {}", id);
            throw new AccessDeniedException("Bạn không có quyền xóa repository này");
        }
        if (azureStorageService.deleteContainer(repo.getContainerName())) {
            log.info("{} xóa repository thành công, id: {}", authUser.getEmail(), id);
            // xoa cac thanh vien truoc khi xoa repo
            repoMemberRepo.deleteByRepoId(id);
            repositoryRepo.delete(repo);
        }
    }

    //check owner
    private boolean isOwner(Repo repo, User user) {
        return repo.getOwner().getId().equals(user.getId());
    }

    @Override
    public RepoResponseDto addMemberToRepository(Long repositoryId, Long userId, Set<RepoPermission> permissions) {
        Repo repo = getRepositoryByIdOrThrow(repositoryId);
        User authUser = getAuthUser();
        if (!isOwner(repo, authUser)) {
            log.error("Không có quyền thêm thành viên vào kho lưu trữ, repositoryId: {}, userId: {}", repositoryId, userId);
            throw new UnauthorizedException("Chỉ chủ sở hữu mới có quyền thêm thành viên");
        }
        String sasToken = azureStorageService.generatePermissionForMemberRepo(repo.getContainerName(), permissions);
        User memberAdd = getUserByIdOrThrow(userId);
        RepoMember repoMember = addMemberToRepository(repo, memberAdd, sasToken);
        repoMember.setPermissions(permissions);
        return convertRepositoryToResponse(repo);
    }

    private User getUserByIdOrThrow(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy user, id: {}", id);
            return new ResourceNotFoundException("User not found");
        });
    }

    private Repo getRepositoryByIdOrThrow(Long id) {
        return repositoryRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy repository, id: {}", id);
            return new ResourceNotFoundException("Repository not found");
        });
    }

    private RepoMember addMemberToRepository(Repo repo, User user, String sasToken) {
        RepoMember repoMember = new RepoMember();
        repoMember.setUser(user);
        repoMember.setSasToken(sasToken);
        repoMember.setRepo(repo);
        return repoMemberRepo.save(repoMember);
    }

    private RepoResponseDto convertRepositoryToResponse(Repo repo) {
        RepoResponseDto repoResponseDto = repoMapper.entityToResponse(repo);
        repoResponseDto.setMemberCount(repoMemberRepo.countRepoMemberByRepoId(repo.getId()));
        return repoResponseDto;
    }

    private User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user; //  Trả về entity User nếu principal là User
        } else if (principal instanceof UserDetails userDetails) {
            //  Nếu không phải User, lấy email rồi tìm trong DB
            log.info("Principal is UserDetails");
            return userRepo.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new ResourceNotFoundException("User not found");
    }

}

package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.PermissionRepoDto;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.*;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UnauthorizedException;
import vn.kltn.map.RepositoryMapper;
import vn.kltn.repository.*;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IRepositoryMemberService;
import vn.kltn.service.IRepositoryService;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j(topic = "REPOSITORY_SERVICE")
@RequiredArgsConstructor
@Transactional
public class RepositoryServiceImpl implements IRepositoryService {
    private final IAzureStorageService azureStorageService;
    private final RepositoryMapper repositoryMapper;
    private final RepositoryRepo repositoryRepo;
    private final RepositoryMemberRepo repositoryMemberRepo;
    private final UserRepo userRepo;
    private final RepositoryMemberPermissionRepo repositoryMemberPermissionRepo;
    private final PermissionRepositoryRepo permissionRepositoryRepo;
    private final IRepositoryMemberService repositoryMemberService;

    @Override
    public RepoResponseDto createRepository(RepositoryRequestDto repositoryRequestDto) {
        String uuid = UUID.randomUUID().toString();
        User owner = getAuthUser();
        String containerName = repositoryRequestDto.getName() + "-" + uuid;
        String sasToken = azureStorageService.createContainerForRepository(containerName);
        //create repository
        Repository repository = repositoryMapper.requestToEntity(repositoryRequestDto);
        repository.setOwner(owner);
        repository.setContainerName(containerName);
        repository = repositoryRepo.save(repository);
        //add member to repository
        RepoMember repoMember = addMemberToRepository(repository, owner, sasToken);
        return convertRepositoryToResponse(repository);
    }

    @Override
    public void deleteRepository(Long id) {
        Repository repository = getRepositoryByIdOrThrow(id);
        User authUser = getAuthUser();
        if (!isOwner(repository, authUser)) {
            log.error("Không có quyền xóa repository, id: {}", id);
            throw new AccessDeniedException("Bạn không có quyền xóa repository này");
        }
        if (azureStorageService.deleteContainer(repository.getContainerName())) {
            log.info("{} xóa repository thành công, id: {}", authUser.getEmail(), id);
            repositoryRepo.delete(repository);
        }
    }

    //check owner
    private boolean isOwner(Repository repository, User user) {
        return repository.getOwner().getId().equals(user.getId());
    }

    @Override
    public RepoResponseDto addMemberToRepository(Long repositoryId, Long userId, List<RepoPermission> permissions) {
        Repository repository = getRepositoryByIdOrThrow(repositoryId);
        User authUser = getAuthUser();
        User user = getUserByIdOrThrow(userId);
        String sasToken = azureStorageService.generatePermissionForMemberRepo(repository.getContainerName(), permissions);
        if(!isOwner(repository, authUser)){
            log.error("Không có quyền thêm thành viên vào repository, repositoryId: {}, userId: {}", repositoryId, userId);
            throw new UnauthorizedException("Bạn không có quyền thêm thành viên vào repository này");
        }
        RepoMember repoMember = addMemberToRepository(repository, user, sasToken);
        addPermissionForMember(repoMember, permissions);
        return convertRepositoryToResponse(repository);
    }

    private User getUserByIdOrThrow(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy user, id: {}", id);
            return new ResourceNotFoundException("User not found");
        });
    }

    private Repository getRepositoryByIdOrThrow(Long id) {
        return repositoryRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy repository, id: {}", id);
            return new ResourceNotFoundException("Repository not found");
        });
    }

    private RepoMember addMemberToRepository(Repository repository, User user, String sasToken) {
        RepoMember repoMember = new RepoMember();
        repoMember.setUser(user);
        repoMember.setSasToken(sasToken);
        repoMember.setRepository(repository);
        return repositoryMemberRepo.save(repoMember);
    }


    private void addPermissionForMember(RepoMember repoMember, List<RepoPermission> permissions) {
        for (RepoPermission permission : permissions) {
            MemberPermission memberPermission = new MemberPermission();
            memberPermission.setMember(repoMember);
            PermissionRepo permissionRepo = permissionRepositoryRepo.findByPermission(permission).orElseThrow(() -> {
                log.error("Không tìm thấy permission, permission: {}", permission);
                return new ResourceNotFoundException("Permission not found");
            });
            memberPermission.setPermission(permissionRepo);
            repositoryMemberPermissionRepo.save(memberPermission);
        }
    }

    private RepoResponseDto convertRepositoryToResponse(Repository repository) {
        RepoResponseDto repoResponseDto = repositoryMapper.entityToResponse(repository);
        repoResponseDto.setOwnerName(repository.getOwner().getFullName());
        repoResponseDto.setOwnerId(repository.getOwner().getId());
        repoResponseDto.setOwnerEmail(repository.getOwner().getEmail());
        return repoResponseDto;
    }

    private User getAuthUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error("Không tìm thấy user, email: {}", email);
            return new ResourceNotFoundException("User not found");
        });
    }
}

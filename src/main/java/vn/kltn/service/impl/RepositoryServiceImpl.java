package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepositoryResponseDto;
import vn.kltn.entity.*;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.RepositoryMapper;
import vn.kltn.repository.*;
import vn.kltn.service.IAzureStorageService;
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


    @Override
    public RepositoryResponseDto createRepository(RepositoryRequestDto repositoryRequestDto) {
        String uuid = UUID.randomUUID().toString();
        User owner = getAuthUser();
        String sasToken = azureStorageService.createContainerForRepository(repositoryRequestDto.getName(), uuid);
        //create repository
        Repository repository = repositoryMapper.requestToEntity(repositoryRequestDto);
        repository.setUuid(uuid);
        repository.setOwner(owner);
        repository = repositoryRepo.save(repository);
        //add member to repository
        RepositoryMember repositoryMember = addMemberToRepository(repository, owner, sasToken);
        return convertRepositoryToResponse(repository);
    }

    @Override
    public void deleteRepository(Long id) {
        Repository repository = repositoryRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy repository, id: {}", id);
            return new ResourceNotFoundException("Repository not found");
        });
        // xoa tbl member
        // xoa tbl member permission
        // kiem tra xem da xoa container thanh cong thi moi xoa repository
        if (azureStorageService.deleteContainer(repository.getUuid())) {
            repositoryRepo.delete(repository);
        }
    }

    private RepositoryMember addMemberToRepository(Repository repository, User user, String sasToken) {
        RepositoryMember repositoryMember = new RepositoryMember();
        repositoryMember.setUser(user);
        repositoryMember.setSasToken(sasToken);
        repositoryMember.setRepository(repository);
        return repositoryMemberRepo.save(repositoryMember);
    }


    private void addPermissionForMember(RepositoryMember repositoryMember, List<RepoPermission> permissions) {
        for (RepoPermission permission : permissions) {
            RepositoryMemberPermission repositoryMemberPermission = new RepositoryMemberPermission();
            repositoryMemberPermission.setMember(repositoryMember);
            PermissionRepository permissionRepository = permissionRepositoryRepo.findByPermission(permission).orElseThrow(() -> {
                log.error("Không tìm thấy permission, permission: {}", permission);
                return new ResourceNotFoundException("Permission not found");
            });
            repositoryMemberPermission.setPermission(permissionRepository);
            repositoryMemberPermissionRepo.save(repositoryMemberPermission);
        }
    }

    private RepositoryResponseDto convertRepositoryToResponse(Repository repository) {
        RepositoryResponseDto repositoryResponseDto = repositoryMapper.entityToResponse(repository);
        repositoryResponseDto.setOwnerName(repository.getOwner().getFullName());
        repositoryResponseDto.setOwnerId(repository.getOwner().getId());
        repositoryResponseDto.setOwnerEmail(repository.getOwner().getEmail());
        return repositoryResponseDto;
    }

    private User getAuthUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error("Không tìm thấy user, email: {}", email);
            return new ResourceNotFoundException("User not found");
        });
    }
}

package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.RepositoryRequestDto;
import vn.kltn.dto.response.RepositoryResponseDto;
import vn.kltn.entity.Repository;
import vn.kltn.entity.RepositoryMember;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.RepositoryMapper;
import vn.kltn.repository.RepositoryMemberRepo;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IRepositoryService;

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
        addMemberToRepository(repository, owner, sasToken);
        return convertRepositoryToResponse(repository);
    }

    private RepositoryMember addMemberToRepository(Repository repository, User user, String sasToken) {
        RepositoryMember repositoryMember = new RepositoryMember();
        repositoryMember.setUser(user);
        repositoryMember.setSasToken(sasToken);
        repositoryMember.setRepository(repository);
        return repositoryMemberRepo.save(repositoryMember);
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

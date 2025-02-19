package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoPermission;
import vn.kltn.common.RepoPermissionDefaults;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.RepoMapper;
import vn.kltn.repository.RepoMemberRepo;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IRepoService;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j(topic = "REPOSITORY_SERVICE")
@RequiredArgsConstructor
@Transactional
public class RepoServiceImpl implements IRepoService {
    @Value("${repo.max-size-gb}")
    private int maxSizeInGB;
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
        repo.setMaxSizeInGB(maxSizeInGB);
        repo = repositoryRepo.save(repo);
        return repo;
    }

    @Override
    public void deleteRepository(Long id) {
        Repo repo = getRepositoryByIdOrThrow(id);
        User authUser = getAuthUser();
        if (!isOwnerRepo(repo, authUser)) {
            log.error("{} Không có quyền xóa repository, id: {}", authUser.getEmail(), id);
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
    private boolean isOwnerRepo(Repo repo, User user) {
        return repo.getOwner().getId().equals(user.getId());
    }

    @Override
    public RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest) {
        // validate quyen cua thanh vien dang dang nhap
        validateSelfPermission(repoId, RepoPermission.ADD_MEMBER);
        // ko the thuc hien hanh dong voi chinh minh
        validateNotSelfRepoMember(userId);
        // validate thanh vien se them da ton tai hay chua
        validateMemberNotExists(userId, repoId);
        Repo repo = getRepositoryByIdOrThrow(repoId);
        // set permission cho thanh vien
        Set<RepoPermission> permissions = determinePermissions(repo, permissionRequest);
        // tao sas token cho thanh vien
        String sasToken = azureStorageService.generatePermissionForMemberRepo(repo.getContainerName(), permissions);
        User memberAdd = getUserByIdOrThrow(userId);
        // save vao database
        addMemberToRepository(repo, memberAdd, permissions, sasToken);
        return convertRepositoryToResponse(repo);
    }

    /**
     * @param repo                 : repository cua thanh vien
     * @param requestedPermissions : neu la owner thi dung permission cua request, nguoc lai dung permission mac dinh
     * @return : nếu authUser là owner thì trả về permission của request, ngược lại trả về permission mặc định
     */
    private Set<RepoPermission> determinePermissions(Repo repo, Set<RepoPermission> requestedPermissions) {
        User authUser = getAuthUser();
        if (isOwnerRepo(repo, authUser)) {
            return requestedPermissions;
        }
        return RepoPermissionDefaults.DEFAULT_MEMBER_PERMISSIONS;
    }

    @Override
    public void removeMemberFromRepository(Long repoId, Long memberId) {
        // validate quyen cua thanh vien
        validateSelfPermission(repoId, RepoPermission.REMOVE_MEMBER);
        repoMemberRepo.deleteById(memberId);
    }


    @Override
    public RepoResponseDto updatePermissionForMember(Long repoId, Long userId, Set<RepoPermission> permissionRequest) {
        // validate quyen cua thanh vien
        validateSelfPermission(repoId, RepoPermission.UPDATE_MEMBER_PERMISSION);
        RepoMember repoMember = getRepoMemberByUserIdAndRepoIdOrThrow(userId, repoId);
        // ko the thuc hien hanh dong voi chinh minh
        validateNotSelfRepoMember(userId);
        repoMember.setPermissions(permissionRequest);
        return convertRepositoryToResponse(repoMember.getRepo());
    }

    public void validateNotSelfRepoMember(Long userIdToCheck) {
        User authUser = getAuthUser(); // Lấy user đang đăng nhập
        if (authUser.getId().equals(userIdToCheck)) {
            throw new InvalidDataException("Không thể thực hiện hành động này với chính mình");
        }
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

    private RepoMember addMemberToRepository(Repo repo, User user, Set<RepoPermission> permissions, String sasToken) {
        RepoMember repoMember = new RepoMember();
        repoMember.setUser(user);
        repoMember.setSasToken(sasToken);
        repoMember.setRepo(repo);
        repoMember.setPermissions(permissions);
        return repoMemberRepo.save(repoMember);
    }

    /**
     * @param repoId     : repo mà thành viên tham gia
     * @param permission : quyền hạn cần kiểm tra với repo và thành viên xác định
     */
    private void validateSelfPermission(Long repoId, RepoPermission permission) {
        User authUser = getAuthUser(); // lay user dang nhap
        Repo repo = getRepositoryByIdOrThrow(repoId);
        // kiem tra xem user co phai la owner cua repository hay khong, neu phai thi tra ve luon, mac dinh owner la co all permission
        if (isOwnerRepo(repo, authUser)) {
            return;
        }
        // kiem tra xem user co phai la member cua repository hay khong
        RepoMember repoMember = getRepoMemberByUserIdAndRepoIdOrThrow(authUser.getId(), repoId);
        // kiem tra xem quyen can kiem tra co ton tai trong danh sach quyen cua thanh vien hay khong
        if (!repoMember.getPermissions().contains(permission)) {
            log.error("{} Không có quyền thực hiện hành động này, userId: {}, repoId: {}", authUser.getEmail(), authUser.getId(), repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }

    /**
     * @param userId : id của user sẽ thêm vào repository
     * @param repoId : id của repository mà user sẽ tham gia
     */
    private void validateMemberNotExists(Long userId, Long repoId) {
        RepoMember repoMember = repoMemberRepo.findRepoMemberByUserIdAndRepoId(userId, repoId).orElseThrow(() -> {
            log.error("Thành viên đã tồn tại, userId: {}, repoId: {}", userId, repoId);
            return new ConflictResourceException("Người dùng này đã là thành viên");
        });
        User authUser = getAuthUser();
        if (isOwnerRepo(repoMember.getRepo(), authUser)) {
            log.error("Thành viên đã tồn tại, userId: {}, repoId: {}", userId, repoId);
            throw new ConflictResourceException("Người dùng này đã là thành viên");
        }
    }

    private RepoMember getRepoMemberByUserIdAndRepoIdOrThrow(Long userId, Long repoId) {
        return repoMemberRepo.findRepoMemberByUserIdAndRepoId(userId, repoId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, userId: {}, repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên: " + userId);
        });
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
            return userRepo.findByEmail(userDetails.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new ResourceNotFoundException("User not found");
    }

}

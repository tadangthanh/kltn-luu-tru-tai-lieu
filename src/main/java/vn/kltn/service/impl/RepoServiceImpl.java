package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RepoPermission;
import vn.kltn.common.RepoPermissionDefaults;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoMemberInfoResponse;
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
import vn.kltn.service.*;
import vn.kltn.validation.RequireOwner;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "REPOSITORY_SERVICE")
@RequiredArgsConstructor
@Transactional
public class RepoServiceImpl implements IRepoService {
    private final IMailService gmailService;
    @Value("${repo.max-size-gb}")
    private int maxSizeInGB;
    @Value("${repo.max-members}")
    private int maxMembers;
    private final IAzureStorageService azureStorageService;
    private final RepoMapper repoMapper;
    private final RepositoryRepo repositoryRepo;
    private final UserRepo userRepo;
    private final RepoMemberRepo repoMemberRepo;
    private final IJwtService jwtService;
    @Value("${jwt.expirationDayInvitation}")
    private long expiryDayInvitation;
    private final IAuthenticationService authenticationService;

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
        User owner = authenticationService.getAuthUser();
        Repo repo = repoMapper.requestToEntity(repoRequestDto);
        repo.setOwner(owner);
        repo.setContainerName(containerName);
        repo.setMaxSizeInGB(maxSizeInGB);
        repo = repositoryRepo.save(repo);
        return repo;
    }

    @Override
    @RequireOwner
    public void deleteRepository(Long id) {
        Repo repo = getRepositoryByIdOrThrow(id);
        User authUser = authenticationService.getAuthUser();
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
    @RequireOwner
    public RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest) {
        // chi co chu so huu moi co quyen them thanh vien
        Repo repo = getRepositoryByIdOrThrow(repoId);
        // ko the thuc hien hanh dong voi chinh minh
        validateNotSelfRepoMember(userId);
        // validate thanh vien se them da ton tai hay chua
        validateMemberNotExists(userId, repoId);
        //validate so luong gioi han member cua repo
        validateMemberCount(repo);
        // set permission cho thanh vien
        Set<RepoPermission> permissions = determinePermissions(repo, permissionRequest);
        // tao sas token cho thanh vien
        User memberAdd = getUserByIdOrThrow(userId);
        // save vao database
        addMemberToRepository(repo, memberAdd, permissions);
        // gui email moi thanh vien
        RepoResponseDto repoResponseDto = convertRepositoryToResponse(repo);
        sendInvitationEmail(memberAdd, repoResponseDto);
        return repoResponseDto;
    }

    private void validateMemberCount(Repo repo) {
        if (repoMemberRepo.countRepoMemberByRepoId(repo.getId()) >= maxMembers) {
            log.error("Số lượng thành viên vượt quá giới hạn, repoId: {}", repo.getId());
            throw new InvalidDataException("Số lượng thành viên vượt quá giới hạn");
        }
    }

    private void sendInvitationEmail(User memberAdd, RepoResponseDto repo) {
        String token = jwtService.generateToken(TokenType.INVITATION_TOKEN, new HashMap<>(), memberAdd.getEmail());
        gmailService.sendAddMemberToRepo(memberAdd.getEmail(), repo, expiryDayInvitation, token);
    }

    /**
     * @param repo                 : repository cua thanh vien
     * @param requestedPermissions : neu la owner thi dung permission cua request, nguoc lai dung permission mac dinh
     * @return : nếu authUser là owner thì trả về permission của request, ngược lại trả về permission mặc định
     */
    private Set<RepoPermission> determinePermissions(Repo repo, Set<RepoPermission> requestedPermissions) {
        User authUser = authenticationService.getAuthUser();
        if (isOwnerRepo(repo, authUser)) {
            return requestedPermissions;
        }
        return RepoPermissionDefaults.DEFAULT_MEMBER_PERMISSIONS;
    }

    @Override
    @RequireOwner
    public void removeMemberFromRepository(Long repoId, Long memberId) {
        repoMemberRepo.deleteById(memberId);
    }


    @Override
    @RequireOwner
    public RepoResponseDto updatePermissionMember(Long repoId, Long memberId, Set<RepoPermission> permissionRequest) {
        Repo repo = getRepositoryByIdOrThrow(repoId);
        RepoMember repoMember = getRepoMemberByIdOrThrow(memberId);
        String sasToken = azureStorageService.generatePermissionForMemberRepo(repo.getContainerName(), permissionRequest);
        repoMember.setPermissions(permissionRequest);
        repoMember.setSasToken(sasToken);
        repoMemberRepo.save(repoMember);
        return convertRepositoryToResponse(repo);
    }

    @Override
    public RepoResponseDto acceptInvitation(Long repoId, String token) {
        // trich xuat email cua thanh vien tu token
        String emailMemberAdd = jwtService.extractEmail(token, TokenType.INVITATION_TOKEN);
        User memberAdd = getUserByEmailOrThrow(emailMemberAdd);
        Repo repo = getRepositoryByIdOrThrow(repoId);
        RepoMember repoMember = getRepoMemberByUserIdAndRepoIdOrThrow(memberAdd.getId(), repoId);
        if (repoMember.getStatus().equals(MemberStatus.PENDING)) {
            repoMember.setStatus(MemberStatus.ACCEPTED);
            repoMember.setSasToken(azureStorageService.generatePermissionForMemberRepo(repo.getContainerName(), repoMember.getPermissions()));
            repoMemberRepo.save(repoMember);
            return convertRepositoryToResponse(repo);
        }
        throw new InvalidDataException("Không thể chấp nhận lời mời");
    }

    @Override
    public RepoResponseDto rejectInvitation(Long repoId, String email) {
        User userMember = getUserByEmailOrThrow(email);
        RepoMember repoMember = getRepoMemberByUserIdAndRepoIdOrThrow(userMember.getId(), repoId);
        if (repoMember.getStatus().equals(MemberStatus.PENDING)) {
            repoMemberRepo.delete(repoMember);
            return convertRepositoryToResponse(repoMember.getRepo());
        }
        throw new InvalidDataException("Dữ liệu không hợp lệ");
    }

    @Override
    public Set<RepoMemberInfoResponse> getListMember(Long repoId) {
        Set<RepoMember> members = repoMemberRepo.findAllByRepoId(repoId);
        return members.stream().map(this::convertMemberToResponse).collect(Collectors.toSet());
    }

    @Override
    @RequireOwner
    public RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto) {
        Repo repo = getRepositoryByIdOrThrow(repoId);
        repoMapper.updateEntityFromRequest(repoRequestDto, repo);
        repo = repositoryRepo.save(repo);
        return convertRepositoryToResponse(repo);
    }

    private RepoMemberInfoResponse convertMemberToResponse(RepoMember member) {
        RepoMemberInfoResponse response = new RepoMemberInfoResponse();
        response.setId(member.getId());
        response.setMemberName(member.getUser().getFullName());
        response.setMemberEmail(member.getUser().getEmail());
        response.setPermissions(member.getPermissions());
        response.setStatus(member.getStatus());
        return response;
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error("Không tìm thấy user, email: {}", email);
            return new ResourceNotFoundException("User not found");
        });
    }

    private RepoMember getRepoMemberByIdOrThrow(Long memberId) {
        return repoMemberRepo.findById(memberId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, id: {}", memberId);
            return new ResourceNotFoundException("Không tìm thấy thành viên: " + memberId);
        });
    }


    private void validateNotSelfRepoMember(Long userIdToCheck) {
        User authUser = authenticationService.getAuthUser(); // Lấy user đang đăng nhập
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

    private RepoMember addMemberToRepository(Repo repo, User user, Set<RepoPermission> permissions) {
        RepoMember repoMember = new RepoMember();
        repoMember.setUser(user);
        repoMember.setRepo(repo);
        repoMember.setPermissions(permissions);
        repoMember.setStatus(MemberStatus.PENDING);
        return repoMemberRepo.save(repoMember);
    }


    /**
     * @param userId : id của user sẽ thêm vào repository
     * @param repoId : id của repository mà user sẽ tham gia
     */
    private void validateMemberNotExists(Long userId, Long repoId) {
        repoMemberRepo.findRepoMemberByUserIdAndRepoId(userId, repoId).ifPresent(repoMember -> {
            log.error("Thành viên đã tồn tại, userId: {}, repoId: {}", userId, repoId);
            throw new ConflictResourceException("Người dùng này đã là thành viên");
        });
        Repo repo = getRepositoryByIdOrThrow(repoId);
        if (repo.getOwner().getId().equals(userId)) {
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

}

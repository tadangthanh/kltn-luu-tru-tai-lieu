package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RepoPermission;
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
import vn.kltn.map.RepoMemberMapper;
import vn.kltn.repository.RepoMemberRepo;
import vn.kltn.repository.RepositoryRepo;
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
    private final IUserService userService;
    private final RepoMemberRepo repoMemberRepo;
    private final IJwtService jwtService;
    @Value("${jwt.expirationDayInvitation}")
    private long expiryDayInvitation;
    private final IAuthenticationService authenticationService;
    private final RepoMemberMapper repoMemberMapper;
    private final IRepoActivityService repoActivityService;

    @Override
    public RepoResponseDto createRepository(RepoRequestDto repoRequestDto) {
        Repo repo = saveRepo(repoRequestDto);
        // them chu so huu vao bang thanh vien voi tat ca cac quyen
        addMemberToRepository(repo.getId(), repo.getOwner().getId(), Set.of(RepoPermission.values()));
        createAzureContainerForRepository(repo);
        return convertRepositoryToResponse(repo);
    }


    private Repo saveRepo(RepoRequestDto repoRequestDto) {
        Repo repo = mapRequestToRepositoryEntity(repoRequestDto);
        String containerName = generateContainerName(repoRequestDto.getName());
        repo.setContainerName(containerName);
        repo.setMaxSizeInGB(maxSizeInGB);
        repo.setAvailableSizeInGB(maxSizeInGB * 1.0);
        return repositoryRepo.save(repo);
    }

    // ten container phai la duy nhat
    private String generateContainerName(String repositoryName) {
        String uuid = UUID.randomUUID().toString();
        return repositoryName + "-" + uuid + "-" + System.currentTimeMillis();
    }

    // tao container tren azure
    private void createAzureContainerForRepository(Repo repo) {
        azureStorageService.createContainerForRepository(repo.getContainerName());
    }

    private Repo mapRequestToRepositoryEntity(RepoRequestDto repoRequestDto) {
        User owner = authenticationService.getAuthUser();
        Repo repo = repoMapper.requestToEntity(repoRequestDto);
        repo.setOwner(owner);
        return repo;
    }

    @Override
    @RequireOwner
    public void deleteRepository(Long id) {
        Repo repo = getRepositoryById(id);
        // xoa log cua repo
        repoActivityService.deleteActivitiesByRepoId(id);
        String containerName = repo.getContainerName();
        repositoryRepo.delete(repo);
        azureStorageService.deleteContainer(containerName);
    }


    @Override
    @RequireOwner
    public RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest) {
        // chi co chu so huu moi co quyen them thanh vien
        Repo repo = getRepositoryById(repoId);
        // validate thanh vien se them da ton tai hay chua
        validateMemberNotExists(userId, repoId);
        //validate so luong gioi han member cua repo
        validateMemberCount(repo);
        User memberAdd = userService.getUserById(userId);
        // save vao database
        saveMember(repo, memberAdd, permissionRequest);
        RepoResponseDto repoResponseDto = convertRepositoryToResponse(repo);
        // neu thanh vien them vao khong phai la chu so huu thi gui email moi
        if (!repo.getOwner().getId().equals(userId)) {
            sendInvitationEmail(memberAdd, repoResponseDto);
        }
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

    @Override
    @RequireOwner
    public void removeMemberFromRepository(Long repoId, Long memberId) {
        RepoMember repoMember = getRepoMemberById(memberId);
        validateSelfMember(repoMember);
        repoMember.setStatus(MemberStatus.REMOVED);
    }

    private void validateSelfMember(RepoMember member) {
        User userAuth = authenticationService.getAuthUser();
        if (userAuth.getId().equals(member.getUser().getId())) {
            log.error("email {} không thể thực hiện hành động này với mình", userAuth.getEmail());
            throw new InvalidDataException("Bạn không thể thực hiện hành động này với mình");
        }
    }


    @Override
    @RequireOwner
    public RepoResponseDto updatePermissionMember(Long repoId, Long memberId, Set<RepoPermission> requestedPermissions) {
        Repo repo = getRepositoryById(repoId);
        RepoMember repoMember = getRepoMemberById(memberId);
        updateMemberPermissions(repoMember, requestedPermissions, repo.getContainerName());
        return convertRepositoryToResponse(repo);
    }

    private void updateMemberPermissions(RepoMember repoMember, Set<RepoPermission> requestedPermissions, String containerName) {
        String sasToken = generateSasTokenForMember(containerName, requestedPermissions);
        repoMember.setPermissions(requestedPermissions);
        repoMember.setSasToken(sasToken);
        repoMemberRepo.save(repoMember);
    }

    private String generateSasTokenForMember(String containerName, Set<RepoPermission> permissions) {
        return azureStorageService.generatePermissionRepo(containerName, permissions);
    }

    @Override
    public RepoResponseDto acceptInvitation(Long repoId, String token) {
        // trich xuat email cua thanh vien tu token
        String emailMemberAdd = jwtService.extractEmail(token, TokenType.INVITATION_TOKEN);
        User memberAdd = userService.getUserByEmail(emailMemberAdd);
        RepoMember repoMember = getRepoMemberByUserIdAndRepoId(memberAdd.getId(), repoId);
        if (repoMember.getStatus().equals(MemberStatus.PENDING)) {
            repoMember.setStatus(MemberStatus.ACCEPTED);
            updatePermissionMember(repoId, repoMember.getId(), repoMember.getPermissions());
            return convertRepositoryToResponse(repoMember.getRepo());
        }
        log.error("Không thể chấp nhận lời mời");
        throw new InvalidDataException("Không thể chấp nhận lời mời");
    }


    @Override
    public RepoResponseDto rejectInvitation(Long repoId, String email) {
        User userMember = userService.getUserByEmail(email);
        RepoMember repoMember = getRepoMemberByUserIdAndRepoId(userMember.getId(), repoId);
        if (repoMember.getStatus().equals(MemberStatus.PENDING)) {
            repoMemberRepo.delete(repoMember);
            return convertRepositoryToResponse(repoMember.getRepo());
        }
        log.error("Dữ liệu không hợp lệ");
        throw new InvalidDataException("Dữ liệu không hợp lệ");
    }

    @Override
    public Set<RepoMemberInfoResponse> getListMember(Long repoId) {
        Set<RepoMember> members = repoMemberRepo.findAllByRepoId(repoId);
        return members.stream().map(repoMemberMapper::toRepoMemberInfoResponse).collect(Collectors.toSet());
    }

    @Override
    @RequireOwner
    public RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto) {
        Repo repo = getRepositoryById(repoId);
        repoMapper.updateEntityFromRequest(repoRequestDto, repo);
        repo = repositoryRepo.save(repo);
        return convertRepositoryToResponse(repo);
    }

    @Override
    public Set<RepoPermission> getPermissionMemberAuthByRepoId(Long repoId) {
        User userAuth = authenticationService.getAuthUser();
        RepoMember repoMember = getRepoMemberByUserIdAndRepoId(userAuth.getId(), repoId);
        return repoMember.getPermissions();
    }

    @Override
    public RepoMember getRepoMemberById(Long memberId) {
        return repoMemberRepo.findById(memberId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, id: {}", memberId);
            return new ResourceNotFoundException("Không tìm thấy thành viên: " + memberId);
        });
    }

    @Override
    public boolean hasPermission(Long repoId, Long userId, RepoPermission permission) {
        RepoMember repoMember = getRepoMemberByUserIdAndRepoId(userId, repoId);
        return repoMember.getPermissions().contains(permission);
    }



    @Override
    public Repo getRepositoryById(Long id) {
        return repositoryRepo.findById(id).orElseThrow(() -> {
            log.error("Không tìm thấy repository, id: {}", id);
            return new ResourceNotFoundException("Repository not found");
        });
    }

    private void saveMember(Repo repo, User user, Set<RepoPermission> permissions) {
        RepoMember repoMember = new RepoMember();
        repoMember.setUser(user);
        repoMember.setRepo(repo);
        repoMember.setPermissions(permissions);
        MemberStatus status = getMemberStatus(repo, user);
        repoMember.setStatus(status);
        if (status.equals(MemberStatus.ACCEPTED)) {
            repoMember.setSasToken(getSasToken(repo, permissions));
        }
        repoMemberRepo.save(repoMember);
    }

    private boolean isOwner(Repo repo, User user) {
        return repo.getOwner().getId().equals(user.getId());
    }

    private String getSasToken(Repo repo, Set<RepoPermission> permissions) {
        return azureStorageService.generatePermissionRepo(repo.getContainerName(), permissions);
    }

    private MemberStatus getMemberStatus(Repo repo, User user) {
        // neu member la chu so huu thi luon la accepted
        if (isOwner(repo, user)) {
            return MemberStatus.ACCEPTED;
        }
        return MemberStatus.PENDING;
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
    }

    @Override
    public RepoMember getRepoMemberByUserIdAndRepoId(Long userId, Long repoId) {
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

package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RepoPermission;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.RepoMapper;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.validation.RequireOwnerRepo;
import vn.kltn.validation.RequireRepoMemberActive;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final IJwtService jwtService;
    @Value("${jwt.expirationDayInvitation}")
    private long expiryDayInvitation;
    private final IAuthenticationService authenticationService;
    private final IRepoActivityService repoActivityService;
    private final IRepoMemberService repoMemberService;
    private final RepoCommonService repoCommonService;

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
    @RequireOwnerRepo
    public void deleteRepository(Long id) {
        Repo repo = getRepositoryById(id);
        // xoa log cua repo
        repoActivityService.deleteActivitiesByRepoId(id);
        String containerName = repo.getContainerName();
        repositoryRepo.delete(repo);
        azureStorageService.deleteContainer(containerName);
    }

    @Override
    @RequireOwnerRepo
    public RepoMemberInfoResponse disableMemberByRepoIdAndUserId(Long repoId, Long userId) {
        RepoMember repoMember = repoMemberService.getMemberActiveByRepoIdAndUserId(repoId, userId);
        validateNotSelfMember(repoMember);
        repoMember.setStatus(MemberStatus.DISABLED);
        return repoMemberService.toRepoMemberInfoResponse(repoMember);
    }

    @Override
    @RequireOwnerRepo
    public RepoMemberInfoResponse enableMemberByRepoIdAndUserId(Long repoId, Long userId) {
        RepoMember repoMember = repoMemberService.getMemberWithStatus(repoId, userId, MemberStatus.DISABLED);
        validateNotSelfMember(repoMember);
        repoMember.setStatus(MemberStatus.ACTIVE);
        return repoMemberService.toRepoMemberInfoResponse(repoMember);
    }

    @Override
    public RepoMemberInfoResponse leaveRepo(Long repoId) {
        RepoMember repoMember = repoMemberService.getAuthMemberWithRepoId(repoId);
        validateMemberNotOwner(repoMember);
        validateMemberNotRemoved(repoMember);
        validateMemberNotExited(repoMember);
        repoMember.setStatus(MemberStatus.EXITED);
        return repoMemberService.toRepoMemberInfoResponse(repoMember);
    }


    private void validateMemberNotOwner(RepoMember repoMember) {
        if (repoMember.getRepo().getOwner().getId().equals(repoMember.getUser().getId())) {
            log.error("Không thể thực hiện hành động này với chủ sở hữu");
            throw new InvalidDataException("Không thể thực hiện hành động này với chủ sở hữu");
        }
    }

    private void validateMemberNotRemoved(RepoMember repoMember) {
        if (repoMember.getStatus().equals(MemberStatus.REMOVED)) {
            log.error("Thành viên đã bị xóa");
            throw new InvalidDataException("Thành viên đã bị xóa");
        }
    }

    private void validateMemberNotExited(RepoMember repoMember) {
        if (repoMember.getStatus().equals(MemberStatus.EXITED)) {
            log.error("Thành viên đã rời khỏi");
            throw new InvalidDataException("Thành viên đã rời khỏi");
        }
    }

    private void validateMemberNotExistByRepoIdAndUserId(Long repoId, Long userId) {
        // validate thanh vien se them da ton tai hay chua
        if (repoMemberService.isExistMemberActiveByRepoIdAndUserId(repoId, userId)) {
            log.error("Thành viên đã tồn tại, userId: {}, repoId: {}", userId, repoId);
            throw new ConflictResourceException("Người dùng này đã là thành viên");
        }
    }

    private void validateNumberOfMembers(Repo repo) {
        if (repoMemberService.countMemberByRepoId(repo.getId()) >= maxMembers) {
            log.error("Repo đã đủ thành viên, không thể thêm");
            throw new ConflictResourceException("Repo đã đủ thành viên, không thể thêm");
        }
    }

    @Override
    @RequireOwnerRepo
    public RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest) {
        // chi co chu so huu moi co quyen them thanh vien
        validateMemberNotExistByRepoIdAndUserId(repoId, userId);
        validateNumberOfMembers(getRepositoryById(repoId));
        Repo repo = getRepositoryById(repoId);
        User memberAdd = userService.getUserById(userId);
        // save vao database
        repoMemberService.saveMemberRepoWithPermission(repo, memberAdd, permissionRequest);
        RepoResponseDto repoResponseDto = convertRepositoryToResponse(repo);
        // neu thanh vien them vao khong phai la chu so huu thi gui email moi
        if (!repo.getOwner().getId().equals(userId)) {
            sendInvitationEmail(memberAdd, repoResponseDto);
        }
        return repoResponseDto;
    }

    private void sendInvitationEmail(User memberAdd, RepoResponseDto repo) {
        String token = jwtService.generateToken(TokenType.INVITATION_TOKEN, new HashMap<>(), memberAdd.getEmail());
        gmailService.sendAddMemberToRepo(memberAdd.getEmail(), repo, expiryDayInvitation, token);
    }

    @Override
    @RequireOwnerRepo
    public void removeMemberByRepoIdAndUserId(Long repoId, Long userId) {
        RepoMember repoMember = repoMemberService.getMemberByRepoIdAndUserId(repoId, userId);
        validateNotSelfMember(repoMember);
        repoMember.setStatus(MemberStatus.REMOVED);
    }

    private void validateNotSelfMember(RepoMember member) {
        User userAuth = authenticationService.getAuthUser();
        if (userAuth.getId().equals(member.getUser().getId())) {
            log.error("email {} không thể thực hiện hành động này với mình", userAuth.getEmail());
            throw new InvalidDataException("Bạn không thể thực hiện hành động này với mình");
        }
    }


    @Override
    @RequireOwnerRepo
    public RepoMemberInfoResponse updatePermissionMemberByRepoIdAndUserId(Long repoId, Long userId, Set<RepoPermission> requestedPermissions) {
        Repo repo = getRepositoryById(repoId);
        RepoMember repoMember = repoMemberService.getMemberByRepoIdAndUserId(repoId, userId);
        repoMember = repoMemberService.updateMemberPermissions(repoMember, requestedPermissions, repo.getContainerName());
        return repoMemberService.toRepoMemberInfoResponse(repoMember);
    }

    @Override
    public RepoResponseDto acceptInvitation(Long repoId, String token) {
        // trich xuat email cua thanh vien tu token
        User memberAdd = getUserByToken(token, TokenType.INVITATION_TOKEN);
        RepoMember repoMember = repoMemberService.getMemberByRepoIdAndUserId(repoId, memberAdd.getId());
        validateMemberInvited(repoMember);
        repoMember.setStatus(MemberStatus.ACTIVE);
        updatePermissionMemberByRepoIdAndUserId(repoId, memberAdd.getId(), repoMember.getPermissions());
        log.info("Chấp nhận lời mời thành công user id{}: ", memberAdd.getId());
        return convertRepositoryToResponse(repoMember.getRepo());
    }

    private User getUserByToken(String token, TokenType tokenType) {
        String email = jwtService.extractEmail(token, tokenType);
        return userService.getUserByEmail(email);
    }

    private void validateMemberInvited(RepoMember repoMember) {
        if (!repoMember.getStatus().equals(MemberStatus.INVITED)) {
            log.error("Thành viên không được mời");
            throw new InvalidDataException("Thành viên không được mời");
        }
    }


    @Override
    public RepoResponseDto rejectInvitation(Long repoId, String email) {
        User userMember = userService.getUserByEmail(email);
        RepoMember repoMember = repoMemberService.getMemberByRepoIdAndUserId(repoId, userMember.getId());
        validateMemberInvited(repoMember);
        repoMemberService.deleteMemberByRepoIdAndUserId(repoId, userMember.getId());
        return convertRepositoryToResponse(repoMember.getRepo());
    }


    @Override
    @RequireOwnerRepo
    public RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto) {
        Repo repo = getRepositoryById(repoId);
        repoMapper.updateEntityFromRequest(repoRequestDto, repo);
        repo = repositoryRepo.save(repo);
        return convertRepositoryToResponse(repo);
    }


    @Override
    public boolean hasPermission(Long repoId, Long userId, RepoPermission permission) {
        RepoMember repoMember = repoMemberService.getMemberActiveByRepoIdAndUserId(repoId, userId);
        return repoMember.getPermissions().contains(permission);
    }


    @Override
    @RequireRepoMemberActive
    public PageResponse<List<RepoMemberInfoResponse>> getListMemberByRepoId(Long repoId, Pageable pageable) {
        Page<RepoMember> repoMemberPage = repoMemberService.getPageMember(repoId, pageable);
        return PaginationUtils.convertToPageResponse(repoMemberPage, pageable, repoMemberService::toRepoMemberInfoResponse);
    }

    @Override
    @RequireRepoMemberActive
    public Repo getRepositoryById(Long id) {
        return repoCommonService.getRepositoryById(id);
    }

    private RepoResponseDto convertRepositoryToResponse(Repo repo) {
        RepoResponseDto repoResponseDto = repoMapper.entityToResponse(repo);
        repoResponseDto.setMemberCount(repoMemberService.countMemberByRepoId(repo.getId()));
        return repoResponseDto;
    }

}

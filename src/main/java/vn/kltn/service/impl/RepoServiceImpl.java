package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RoleName;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.entity.Member;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.RepoMapper;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.validation.HasAnyRole;
import vn.kltn.validation.RequireRepoMemberActive;

import java.util.HashMap;
import java.util.List;
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
    private final IMemberService memberService;
    private final RepoCommonService repoCommonService;

    @Override
    public RepoResponseDto createRepository(RepoRequestDto repoRequestDto) {
        Repo repo = saveRepo(repoRequestDto);
        // them chu so huu vao bang thanh vien voi quyen admin
        memberService.saveMemberWithRoleAdmin(repo, repo.getOwner());
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
    @HasAnyRole(RoleName.ADMIN)
    public void deleteRepository(Long id) {
        Repo repo = getRepositoryById(id);
        // xoa log cua repo
        repoActivityService.deleteActivitiesByRepoId(id);
        String containerName = repo.getContainerName();
        repositoryRepo.delete(repo);
        azureStorageService.deleteContainer(containerName);
    }


    private void validateMemberNotExistByRepoIdAndUserId(Long repoId, Long userId) {
        // validate thanh vien se them da ton tai hay chua
        if (memberService.isExistMemberActiveByRepoIdAndUserId(repoId, userId)) {
            log.error("Thành viên đã tồn tại, userId: {}, repoId: {}", userId, repoId);
            throw new ConflictResourceException("Người dùng này đã là thành viên");
        }
    }

    private void validateNumberOfMembers(Repo repo) {
        if (memberService.countMemberByRepoId(repo.getId()) >= maxMembers) {
            log.error("Repo đã đủ thành viên, không thể thêm");
            throw new ConflictResourceException("Repo đã đủ thành viên, không thể thêm");
        }
    }

    @Override
    @HasAnyRole({RoleName.ADMIN})
    public RepoResponseDto addMemberToRepository(Long repoId, Long userId, Long roleId) {
        validateRepoExist(repoId);
        validateMemberNotExistByRepoIdAndUserId(repoId, userId);
        validateNumberOfMembers(getRepositoryById(repoId));
        Repo repo = getRepositoryById(repoId);
        User memberAdd = userService.getUserById(userId);
        // save vao database
        memberService.saveMemberWithRoleId(repo, memberAdd, roleId);
        RepoResponseDto repoResponseDto = convertRepositoryToResponse(repo);
        sendInvitationEmail(memberAdd, repoResponseDto);
        return repoResponseDto;
    }

    private void validateRepoExist(Long repoId) {
        if (!repositoryRepo.existsById(repoId)) {
            log.error("Kho lưu trữ không tồn tại, id: {}", repoId);
            throw new InvalidDataException("Kho lưu trữ không tồn tại");
        }
    }

    private void sendInvitationEmail(User memberAdd, RepoResponseDto repo) {
        String token = jwtService.generateToken(TokenType.INVITATION_TOKEN, new HashMap<>(), memberAdd.getEmail());
        gmailService.sendAddMemberToRepo(memberAdd.getEmail(), repo, expiryDayInvitation, token);
    }


    @Override
    public RepoResponseDto acceptInvitation(Long repoId, String token) {
        log.info("Chấp nhận lời mời vào kho lưu trữ, repoId: {}, token: {}", repoId, token);
        // trich xuat email cua thanh vien tu token
        User memberAdd = getUserByToken(token, TokenType.INVITATION_TOKEN);
        Member member = memberService.getMemberByRepoIdAndUserId(repoId, memberAdd.getId());
        validateMemberInvited(member);
        member.setStatus(MemberStatus.ACTIVE);
        log.info("Chấp nhận lời mời thành công user id{}: ", memberAdd.getId());
        return convertRepositoryToResponse(member.getRepo());
    }

    private User getUserByToken(String token, TokenType tokenType) {
        String email = jwtService.extractEmail(token, tokenType);
        return userService.getUserByEmail(email);
    }

    private void validateMemberInvited(Member member) {
        if (!member.getStatus().equals(MemberStatus.INVITED)) {
            log.error("Thành viên không được mời");
            throw new InvalidDataException("Thành viên không được mời");
        }
    }


    @Override
    public RepoResponseDto rejectInvitation(Long repoId, String email) {
        User userMember = userService.getUserByEmail(email);
        Member member = memberService.getMemberByRepoIdAndUserId(repoId, userMember.getId());
        validateMemberInvited(member);
        memberService.deleteMemberByRepoIdAndUserId(repoId, userMember.getId());
        return convertRepositoryToResponse(member.getRepo());
    }


    @Override
    @HasAnyRole(RoleName.ADMIN)
    public RepoResponseDto update(Long repoId, RepoRequestDto repoRequestDto) {
        Repo repo = getRepositoryById(repoId);
        repoMapper.updateEntityFromRequest(repoRequestDto, repo);
        repo = repositoryRepo.save(repo);
        return convertRepositoryToResponse(repo);
    }


    @Override
    public boolean userHasAnyRoleRepoId(Long repoId, Long userId, RoleName[] listRole) {
        Member member = memberService.getMemberActiveByRepoIdAndUserId(repoId, userId);
        return userHasAnyRole(member, listRole);
    }

    private boolean userHasAnyRole(Member member, RoleName[] listRole) {
        for (RoleName roleName : listRole) {
            if (member.getRole().getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PageResponse<List<RepoResponseDto>> getPageResponseByUserAuth(Pageable pageable) {
        User userAuth = authenticationService.getAuthUser();
        Page<Repo> repoPage = repositoryRepo.findAllByUserIdActive(userAuth.getId(), pageable);
        return PaginationUtils.convertToPageResponse(repoPage, pageable, this::convertRepositoryToResponse);
    }

    @Override
    @RequireRepoMemberActive
    public Repo getRepositoryById(Long repoId) {
        return repoCommonService.getRepositoryById(repoId);
    }

    private RepoResponseDto convertRepositoryToResponse(Repo repo) {
        RepoResponseDto repoResponseDto = repoMapper.entityToResponse(repo);
        repoResponseDto.setMemberCount(memberService.countMemberByRepoId(repo.getId()));
        return repoResponseDto;
    }

}

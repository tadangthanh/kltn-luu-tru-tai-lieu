package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.RepoMapper;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.validation.HasAnyRole;
import vn.kltn.validation.RequireMemberActive;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j(topic = "REPOSITORY_SERVICE")
@RequiredArgsConstructor
@Transactional
public class RepoServiceImpl implements IRepoService {
    private final IAzureStorageService azureStorageService;
    private final RepoMapper repoMapper;
    private final RepositoryRepo repositoryRepo;
    private final IUserService userService;
    private final IJwtService jwtService;
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
        Repo repo = mapRepoRequestToEntity(repoRequestDto);
        String containerName = generateContainerName(repoRequestDto.getName());
        repo.setContainerName(containerName);
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

    private Repo mapRepoRequestToEntity(RepoRequestDto repoRequestDto) {
        User owner = authenticationService.getCurrentUser();
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
    public PageResponse<List<RepoResponseDto>> getPageRepoResponseByUserAuth(Pageable pageable) {
        User userAuth = authenticationService.getCurrentUser();
        Page<Repo> repoPage = repositoryRepo.findAllByUserIdActive(userAuth.getId(), pageable);
        return PaginationUtils.convertToPageResponse(repoPage, pageable, this::convertRepositoryToResponse);
    }

    @Override
    @RequireMemberActive
    public Repo getRepositoryById(Long repoId) {
        return repoCommonService.getRepositoryById(repoId);
    }

    private RepoResponseDto convertRepositoryToResponse(Repo repo) {
        RepoResponseDto repoResponseDto = repoMapper.entityToResponse(repo);
        repoResponseDto.setMemberCount(memberService.countMemberByRepoId(repo.getId()));
        return repoResponseDto;
    }

}

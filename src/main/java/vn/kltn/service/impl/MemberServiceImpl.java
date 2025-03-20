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
import vn.kltn.dto.response.MemberResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Member;
import vn.kltn.entity.MemberRole;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.RepoMemberMapper;
import vn.kltn.repository.RepoMemberRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.util.SasTokenValidator;
import vn.kltn.validation.HasAnyRole;
import vn.kltn.validation.RequireMemberActive;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "MEMBER_SERVICE")
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {
    private final RepoMemberRepo repoMemberRepo;
    private final IAzureStorageService azureStorageService;
    private final IAuthenticationService authenticationService;
    private final RepoCommonService repoCommonService;
    private final RepoMemberMapper repoMemberMapper;
    private final IMemberRoleService memberRoleService;
    private final IMailService gmailService;
    @Value("${repo.max-members}")
    private int maxMembers;
    private final IUserService userService;

    @Override
    @HasAnyRole({RoleName.ADMIN})
    public MemberResponse sendInvitationRepo(Long repoId, Long userId, Long roleId) {
        // xác thực repo tồn tại
        validateRepoExist(repoId);
        // xác thực thành viên chưa tồn tại trong repo
        validateMemberNotExistByRepoIdAndUserId(repoId, userId);
        // xác thực số lượng thành viên
        validateNumberOfMembers(repoId);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        User memberAdd = userService.getUserById(userId);
        // save vao database
        MemberResponse response = saveMemberWithRoleId(repo, memberAdd, roleId);
        gmailService.sendInvitationMember(memberAdd.getEmail(), repo);
        return response;
    }

    private void validateRepoExist(Long repoId) {
        if (!repoCommonService.existsById(repoId)) {
            log.error("Kho lưu trữ không tồn tại, id: {}", repoId);
            throw new InvalidDataException("Kho lưu trữ không tồn tại");
        }
    }

    private void validateMemberNotExistByRepoIdAndUserId(Long repoId, Long userId) {
        // validate thanh vien se them da ton tai hay chua
        if (isExistMemberActiveByRepoIdAndUserId(repoId, userId)) {
            log.error("Thành viên đã tồn tại, userId: {}, repoId: {}", userId, repoId);
            throw new ConflictResourceException("Người dùng này đã là thành viên");
        }
    }

    private void validateNumberOfMembers(Long repoId) {
        if (countMemberByRepoId(repoId) >= maxMembers) {
            log.error("Repo đã đủ thành viên, không thể thêm");
            throw new ConflictResourceException("Repo đã đủ thành viên, không thể thêm");
        }
    }

    @Override
    public Member getAuthMemberWithRepoId(Long repoId) {
        User authUser = authenticationService.getAuthUser();
        return repoMemberRepo.getMemberActiveByRepoIdAndUserId(repoId, authUser.getId()).orElseThrow(() -> {
            log.error("{} không phải thành viên repository: {}", authUser.getEmail(), repoId);
            return new ResourceNotFoundException("Bạn không phải thành viên repository");
        });
    }

    @Override
    public Member updateSasTokenMember(Repo repo, Member member) {
        String newSasToken = azureStorageService.generatePermissionRepoByMemberRole(repo.getContainerName(), member.getRole());
        member.setSasToken(newSasToken);
        return repoMemberRepo.save(member);
    }

    @Override
    public Member getMemberActiveByRepoIdAndUserId(Long repoId, Long userId) {
        return repoMemberRepo.findRepoMemberActiveByRepoIdAndUserId(repoId, userId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên active, userId: {}, trong repo repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public Member getMemberByRepoIdAndUserId(Long repoId, Long userId) {
        return repoMemberRepo.findRepoMemberByRepoIdAndUserId(repoId, userId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, userId: {}, trong repo repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public Member getMemberWithStatus(Long repoId, Long userId, MemberStatus status) {
        return repoMemberRepo.findRepoMemberByRepoIdAndUserIdAndStatus(repoId, userId, status).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên {}, userId: {}, trong repo repoId: {}", status, userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId) {
        return repoMemberRepo.isExistMemberActiveByRepoIdAndUserId(repoId, userId);
    }

    @Override
    public MemberResponse saveMemberWithRoleId(Repo repo, User user, Long roleId) {
        Member member = mapToRepoMember(repo, user);
        MemberRole memberRole = setRoleMemberById(member, roleId);
        member.setSasToken(getSasToken(repo, memberRole));
        member = repoMemberRepo.save(member);
        return toRepoMemberInfoResponse(member);
    }

    private MemberRole setRoleMemberById(Member member, Long roleId) {
        MemberRole memberRole = memberRoleService.getRoleById(roleId);
        member.setRole(memberRole);
        return memberRole;
    }


    @Override
    public void saveMemberWithRoleAdmin(Repo repo, User user) {
        Member member = mapToRepoMember(repo, user);
        MemberRole memberRole = memberRoleService.getRoleByName(RoleName.ADMIN);
        member.setRole(memberRole);
        member.setSasToken(getSasToken(repo, memberRole));
        repoMemberRepo.save(member);
    }


    private String getSasToken(Repo repo, MemberRole role) {
        return azureStorageService.generatePermissionRepoByMemberRole(repo.getContainerName(), role);
    }

    private Member mapToRepoMember(Repo repo, User user) {
        Member member = new Member();
        member.setUser(user);
        member.setRepo(repo);
        MemberStatus status = getMemberStatus(repo, user);
        member.setStatus(status);
        return member;
    }

    @Override
    public int countMemberByRepoId(Long repoId) {
        return repoMemberRepo.countRepoMemberByRepoId(repoId);
    }

    private Member updateMemberPermissions(Member member, MemberRole role, String containerName) {
        String sasToken = generateSasTokenForMember(containerName, role);
        member.setRole(role);
        member.setSasToken(sasToken);
        return repoMemberRepo.save(member);
    }

    @Override
    public void deleteMemberByRepoIdAndUserId(Long repoId, Long userId) {
        Member member = getMemberByRepoIdAndUserId(repoId, userId);
        repoMemberRepo.delete(member);
    }

    private MemberResponse toRepoMemberInfoResponse(Member member) {
        return repoMemberMapper.toRepoMemberInfoResponse(member);
    }

    @Override
    public String getSasTokenByAuthMemberWithRepo(Repo repo) {
        User authUser = authenticationService.getAuthUser();
        Member member = getMemberActiveByRepoIdAndUserId(repo.getId(), authUser.getId());
        String sasToken = member.getSasToken();
        if (!SasTokenValidator.isSasTokenValid(sasToken)) {
            member = updateSasTokenMember(repo, member);
        }
        return member.getSasToken();
    }

    @Override
    public MemberResponse disableMemberByRepoIdAndUserId(Long repoId, Long userId) {
        Member member = getMemberActiveByRepoIdAndUserId(repoId, userId);
        validateNotSelfMember(member);
        member.setStatus(MemberStatus.DISABLED);
        member.setSasToken(null);
        return toRepoMemberInfoResponse(member);
    }

    private void validateNotSelfMember(Member member) {
        User userAuth = authenticationService.getAuthUser();
        if (userAuth.getId().equals(member.getUser().getId())) {
            log.error("email {} không thể thực hiện hành động này với mình", userAuth.getEmail());
            throw new InvalidDataException("Bạn không thể thực hiện hành động này với mình");
        }
    }

    @Override
    public MemberResponse enableMemberByRepoIdAndUserId(Long repoId, Long userId) {
        Member member = getMemberWithStatus(repoId, userId, MemberStatus.DISABLED);
        validateNotSelfMember(member);
        member.setStatus(MemberStatus.ACTIVE);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        member.setSasToken(getSasToken(repo, member.getRole()));
        return toRepoMemberInfoResponse(member);
    }

    @Override
    public MemberResponse leaveRepo(Long repoId) {
        Member member = getAuthMemberWithRepoId(repoId);
        validateMemberNotOwner(member);
        validateMemberNotRemoved(member);
        validateMemberNotExited(member);
        member.setStatus(MemberStatus.EXITED);
        return toRepoMemberInfoResponse(member);
    }

    private void validateMemberNotOwner(Member member) {
        if (member.getRepo().getOwner().getId().equals(member.getUser().getId())) {
            log.error("Không thể thực hiện hành động này với chủ sở hữu");
            throw new InvalidDataException("Không thể thực hiện hành động này với chủ sở hữu");
        }
    }

    private void validateMemberNotRemoved(Member member) {
        if (member.getStatus().equals(MemberStatus.REMOVED)) {
            log.error("Thành viên đã bị xóa");
            throw new InvalidDataException("Thành viên đã bị xóa");
        }
    }

    private void validateMemberNotExited(Member member) {
        if (member.getStatus().equals(MemberStatus.EXITED)) {
            log.error("Thành viên đã rời khỏi");
            throw new InvalidDataException("Thành viên đã rời khỏi");
        }
    }

    @Override
    public void removeMemberByRepoIdAndUserId(Long repoId, Long userId) {
        Member member = getMemberByRepoIdAndUserId(repoId, userId);
        validateNotSelfMember(member);
        member.setStatus(MemberStatus.REMOVED);
    }

    @Override
    @HasAnyRole(RoleName.ADMIN)
    public MemberResponse updateMemberRoleByRepoIdAndUserId(Long repoId, Long userId, Long roleId) {
        Repo repo = repoCommonService.getRepositoryById(repoId);
        Member member = getMemberByRepoIdAndUserId(repoId, userId);
        member = updateMemberPermissions(member, memberRoleService.getRoleById(roleId), repo.getContainerName());
        return toRepoMemberInfoResponse(member);
    }

    @Override
    @RequireMemberActive
    public PageResponse<List<MemberResponse>> getListMemberByRepoId(Long repoId, Pageable pageable) {
        Page<Member> repoMemberPage = repoMemberRepo.findAllByRepoId(repoId, pageable);
        return PaginationUtils.convertToPageResponse(repoMemberPage, pageable, this::toRepoMemberInfoResponse);
    }

    @Override
    public boolean userHasAnyRoleRepoId(Long repoId, Long userId, RoleName[] listRole) {
        Member member = getMemberActiveByRepoIdAndUserId(repoId, userId);
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

    private String generateSasTokenForMember(String containerName, MemberRole memberRole) {
        return azureStorageService.generatePermissionRepoByMemberRole(containerName, memberRole);
    }


    private boolean isOwnerRepo(Repo repo, User user) {
        return repo.getOwner().getId().equals(user.getId());
    }

    private MemberStatus getMemberStatus(Repo repo, User user) {
        // neu member la chu so huu thi luon la active
        if (isOwnerRepo(repo, user)) {
            return MemberStatus.ACTIVE;
        }
        return MemberStatus.INVITED;
    }
}

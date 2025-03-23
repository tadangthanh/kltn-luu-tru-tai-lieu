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
import vn.kltn.repository.MemberRepo;
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
    private final MemberRepo memberRepo;
    private final IAzureStorageService azureStorageService;
    private final IAuthenticationService authenticationService;
    private final RepoCommonService repoCommonService;
    private final RepoMemberMapper repoMemberMapper;
    private final IMemberRoleService memberRoleService;
    private final IMailService gmailService;
    @Value("${repo.max-members-per-repo-default}")
    private int maxMembersDefault; // số lượng thành viên tối đa của 1 repo theo mặc dịnh
    @Value("${repo.max-repos-per-member-default}")
    private int maxRepoPerMembersDefault; // số lượng repo mà 1 user có thể tham gia theo mac dinh
    private final IUserService userService;

    @Override
    @HasAnyRole({RoleName.ADMIN})
    public MemberResponse sendInvitationRepo(Long repoId, Long userId, Long roleId) {
        validateInvitationConditions(repoId, userId, roleId);
        Member newMember = createAndSaveInvitedMember(repoId, userId, roleId);
        sendInvitationEmail(newMember);
        return toRepoMemberInfoResponse(newMember);
    }

    private void validateRoleNotIsAdminById(Long roleId) {
        if (memberRoleService.isRoleAdminByRoleId(roleId)) {
            log.error("Không thể thêm quyền admin cho thành viên");
            throw new InvalidDataException("Không thể thêm quyền admin cho thành viên");
        }
    }

    // Xác thực các điều kiện trước khi gửi lời mời.
    private void validateInvitationConditions(Long repoId, Long userId, Long roleId) {
        // repo phải tồn tại
        validateRepoExist(repoId);
        // thành viên phải chưa có trong repo đó
        validateMemberNotExistByRepoIdAndUserId(repoId, userId);
        // số lượng thành viên không vượt quá giới hạn
        validateNumberOfMembers(repoId);
        // role gán cho thành viên ko được là admin
        validateRoleNotIsAdminById(roleId);
    }

    //Tạo và lưu thành viên với trạng thái INVITED.
    private Member createAndSaveInvitedMember(Long repoId, Long userId, Long roleId) {
        User user = userService.getUserById(userId);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        Member member = saveMemberWithRoleId(repo, user, roleId);
        member.setStatus(MemberStatus.INVITED);
        return memberRepo.save(member);
    }

    //Gửi email mời thành viên.
    private void sendInvitationEmail(Member member) {
        gmailService.sendInvitationMember(member.getUser().getEmail(), member.getRepo());
    }


    private Member saveMemberWithRoleId(Repo repo, User user, Long roleId) {
        Member member = mapToMember(repo, user);
        MemberRole memberRole = setRoleMemberById(member, roleId);
        member.setSasToken(getSasToken(repo, memberRole));
        return memberRepo.save(member);
    }

    private MemberRole setRoleMemberById(Member member, Long roleId) {
        MemberRole memberRole = memberRoleService.getRoleById(roleId);
        member.setRole(memberRole);
        return memberRole;
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
        if (countMemberByRepoId(repoId) >= maxMembersDefault) {
            log.error("Repo đã đủ thành viên, không thể thêm");
            throw new ConflictResourceException("Repository đã đủ thành viên, không thể thêm, bạn có thể nâng cấp gói để thêm thành viên");
        }
    }

    @Override
    public Member getAuthMemberWithRepoId(Long repoId) {
        User authUser = authenticationService.getCurrentUser();
        return memberRepo.getMemberActiveByRepoIdAndUserId(repoId, authUser.getId()).orElseThrow(() -> {
            log.error("{} không phải thành viên repository: {}", authUser.getEmail(), repoId);
            return new ResourceNotFoundException("Bạn không phải thành viên repository");
        });
    }

    @Override
    public Member getMemberById(Long memberId) {
        return memberRepo.findById(memberId).orElseThrow(() -> {
            log.warn("Không tìm thấy thành viên, id: {}", memberId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public Member getMemberByEmailWithRepoId(Long repoId, String email) {
        User user = userService.getUserByEmail(email);
        return memberRepo.getMemberActiveByRepoIdAndUserId(repoId, user.getId()).orElseThrow(() -> {
            log.error("{} không phải thành viên repository: {}", email, repoId);
            return new ResourceNotFoundException("Bạn không phải thành viên repository");
        });
    }

    private Member updateSasTokenMember(Repo repo, Member member) {
        String newSasToken = azureStorageService.generatePermissionRepoByMemberRole(repo.getContainerName(), member.getRole());
        member.setSasToken(newSasToken);
        return memberRepo.save(member);
    }

    private Member getMemberActiveById(Long id) {
        return memberRepo.findMemberActiveById(id).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên active, id: {}", id);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    private Member getMemberActiveByRepoIdAndUserId(Long repoId, Long userId) {
        return memberRepo.findRepoMemberActiveByRepoIdAndUserId(repoId, userId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên active, userId: {}, trong repo repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public Member getMemberByRepoIdAndUserId(Long repoId, Long userId) {
        return memberRepo.findRepoMemberByRepoIdAndUserId(repoId, userId).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, userId: {}, trong repo repoId: {}", userId, repoId);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    private Member getMemberByIdAndStatus(Long id, MemberStatus status) {
        return memberRepo.findMemberByIdAndStatus(id, status).orElseThrow(() -> {
            log.error("Không tìm thấy thành viên, id: {}", id);
            return new ResourceNotFoundException("Không tìm thấy thành viên");
        });
    }

    @Override
    public boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId) {
        return memberRepo.isExistMemberActiveByRepoIdAndUserId(repoId, userId);
    }


    @Override
    public void saveMemberWithRoleAdmin(Repo repo, User user) {
        Member member = mapToMember(repo, user);
        member.setStatus(MemberStatus.ACTIVE);
        MemberRole memberRole = memberRoleService.getRoleByName(RoleName.ADMIN);
        member.setRole(memberRole);
        member.setSasToken(getSasToken(repo, memberRole));
        memberRepo.save(member);
    }

    // tạo sas token của azure storage
    private String getSasToken(Repo repo, MemberRole role) {
        return azureStorageService.generatePermissionRepoByMemberRole(repo.getContainerName(), role);
    }

    private Member mapToMember(Repo repo, User user) {
        Member member = new Member();
        member.setUser(user);
        member.setRepo(repo);
        return member;
    }

    @Override
    public int countMemberByRepoId(Long repoId) {
        return memberRepo.countRepoMemberByRepoId(repoId);
    }


    @Override
    public void deleteMemberByRepoIdAndUserId(Long repoId, Long userId) {
        Member member = getMemberByRepoIdAndUserId(repoId, userId);
        memberRepo.delete(member);
    }

    private MemberResponse toRepoMemberInfoResponse(Member member) {
        return repoMemberMapper.toRepoMemberInfoResponse(member);
    }

    @Override
    public String getSasTokenByAuthMemberWithRepo(Repo repo) {
        User authUser = authenticationService.getCurrentUser();
        Member member = getMemberActiveByRepoIdAndUserId(repo.getId(), authUser.getId());
        String sasToken = member.getSasToken();
        if (!SasTokenValidator.isSasTokenValid(sasToken)) {
            member = updateSasTokenMember(repo, member);
        }
        return member.getSasToken();
    }

    @Override
    @HasAnyRole(RoleName.ADMIN)
    public MemberResponse disableMemberById(Long memberId) {
        Member member = getMemberActiveById(memberId);
        validateNotSelfMember(member);
        member = disableMember(member);
        return toRepoMemberInfoResponse(member);
    }

    private Member disableMember(Member member) {
        member.setStatus(MemberStatus.DISABLED);
        member.setSasToken(null);
        return memberRepo.save(member);
    }

    private void validateNotSelfMember(Member member) {
        User userAuth = authenticationService.getCurrentUser();
        if (userAuth.getId().equals(member.getUser().getId())) {
            log.error("email {} không thể thực hiện hành động này với mình", userAuth.getEmail());
            throw new InvalidDataException("Bạn không thể thực hiện hành động này với mình");
        }
    }

    @Override
    @HasAnyRole(RoleName.ADMIN)
    public MemberResponse enableMemberById(Long memberId) {
        Member member = getMemberByIdAndStatus(memberId, MemberStatus.DISABLED);
        validateNotSelfMember(member);
        member = enableMember(member);
        return toRepoMemberInfoResponse(member);
    }

    private Member enableMember(Member member) {
        member.setStatus(MemberStatus.ACTIVE);
        Repo repo = member.getRepo();
        member.setSasToken(getSasToken(repo, member.getRole()));
        return memberRepo.save(member);
    }

    @Override
    public MemberResponse leaveRepo(Long repoId) {
        Member member = getAuthMemberWithRepoId(repoId);
        validateLeaveConditions(member);
        member.setStatus(MemberStatus.EXITED);
        return toRepoMemberInfoResponse(member);
    }

    private void validateLeaveConditions(Member member) {
        validateMemberNotOwner(member);
        validateMemberNotRemoved(member);
        validateMemberNotExited(member);
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
    @HasAnyRole(RoleName.ADMIN)
    public MemberResponse removeMemberById(Long memberId) {
        Member member = getMemberById(memberId);
        validateNotSelfMember(member);
        validateMemberNotRemoved(member);
        member.setStatus(MemberStatus.REMOVED);
        return toRepoMemberInfoResponse(member);
    }

    @Override
    @HasAnyRole(RoleName.ADMIN)
    public MemberResponse updateMemberRoleById(Long memberId, Long roleId) {
        // role gán cho thành viên ko được là admin
        validateRoleNotIsAdminById(roleId);
        Member member = getMemberById(memberId);
        member = updateRoleMember(member, roleId);
        return toRepoMemberInfoResponse(member);
    }

    private Member updateRoleMember(Member member, Long roleId) {
        MemberRole role = memberRoleService.getRoleById(roleId);
        Repo repo = member.getRepo();
        member.setRole(role);
        member = updateSasTokenMember(repo, member);
        return memberRepo.save(member);
    }

    @Override
    @RequireMemberActive
    public PageResponse<List<MemberResponse>> getListMemberByRepoId(Long repoId, Pageable pageable) {
        Page<Member> repoMemberPage = memberRepo.findAllByRepoId(repoId, pageable);
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
}

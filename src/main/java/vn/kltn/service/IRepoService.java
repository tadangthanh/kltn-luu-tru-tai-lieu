package vn.kltn.service;

import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.dto.response.RepoResponseDto;

import java.util.Set;

public interface IRepoService {
    RepoResponseDto createRepository(RepoRequestDto repoRequestDto);

    void deleteRepository(Long id);

    RepoResponseDto addMemberToRepository(Long repoId, Long userId, Set<RepoPermission> permissionRequest);

    void removeMemberFromRepository(Long repoId, Long memberId);

    RepoResponseDto updatePermissionMember(Long repoId, Long memberId, Set<RepoPermission> requestedPermissions );

    RepoResponseDto acceptInvitation(Long repoId, String token);

    RepoResponseDto rejectInvitation(Long repoId, String email);

    Set<RepoMemberInfoResponse> getListMember(Long repoId);

    RepoResponseDto update(Long repoId,RepoRequestDto repoRequestDto);


//    Quản lý repo: Cập nhật repo, chuyển quyền sở hữu.
//            ✅ Quản lý thành viên: Xem danh sách, kiểm tra quyền, mời qua email.
//✅ Quản lý dung lượng: Kiểm tra dung lượng sử dụng, đặt giới hạn.
//            ✅ Lịch sử hoạt động: Theo dõi thay đổi, audit logs.
//✅ Tìm kiếm và lọc: Dễ dàng tìm và quản lý repo.
//✅ Sao lưu và phục hồi: Đảm bảo an toàn dữ liệu.
}

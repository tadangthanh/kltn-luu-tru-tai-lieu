package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RepoPermission;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class RepoMemberInfoResponse implements Serializable {
    private Long id;
    private Long repoId;
    private Long userId;
    private String memberName;
    private String memberEmail;
    private MemberStatus status;
    private Set<RepoPermission> permissions;
}

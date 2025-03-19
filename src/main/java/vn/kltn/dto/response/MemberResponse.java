package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.MemberStatus;
import vn.kltn.common.RoleName;

import java.io.Serializable;

@Getter
@Setter
public class MemberResponse implements Serializable {
    private Long id;
    private Long repoId;
    private Long userId;
    private String memberName;
    private String memberEmail;
    private MemberStatus status;
    private RoleName role;
    private String roleDescription;
}

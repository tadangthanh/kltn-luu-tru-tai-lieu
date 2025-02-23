package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.MemberStatus;

import java.io.Serializable;

@Getter
@Setter
public class RepoMemberInfoResponse implements Serializable {
    private Long id;
    private String memberName;
    private String memberEmail;
    private MemberStatus status;
}

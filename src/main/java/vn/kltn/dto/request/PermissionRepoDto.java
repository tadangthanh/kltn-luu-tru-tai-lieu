package vn.kltn.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;

import java.io.Serializable;

@Getter
@Setter
public class PermissionRepoDto implements Serializable {
    private RepoPermission permission;
}

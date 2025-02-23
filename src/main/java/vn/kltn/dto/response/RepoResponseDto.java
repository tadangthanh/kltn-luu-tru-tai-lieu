package vn.kltn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.BaseDto;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepoResponseDto extends BaseDto {
    private String name;
    private String description;
    private Long ownerId;
    private String ownerEmail;
    private int memberCount;
    private String ownerName;
//    private Set<RepoPermission> permissions;
}

package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoActionType;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class RepoActivityResponse extends BaseDto {
    private String repoName;
    private String repoId;
    private String authorName;
    private String authorEmail;
    private Long authorId;
    private RepoActionType action;
    private String details;
}

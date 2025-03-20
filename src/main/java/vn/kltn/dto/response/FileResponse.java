package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileResponse extends BaseDto {
    private String fileName;
    private String fileType;
    private String fileBlobName;
    private Long fileSize;
    private String repoName;
    private Long repoId;
    private Boolean isPublic;
    private int version;
    private String description;
    private String checkSum;
    private String uploadedBy;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deletedAt;
}

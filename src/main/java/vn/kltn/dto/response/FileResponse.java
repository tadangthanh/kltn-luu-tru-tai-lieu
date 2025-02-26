package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FileResponse extends BaseDto {
    private String fileName;
    private String fileType;
    private String fileBlobName;
    private String fileUrl;
    private Long fileSize;
    private String repoName;
    private Long repoId;
    private boolean isPublic;
    private int version;
    private String description;
    private String checkSum;
    private String uploadedBy;

}

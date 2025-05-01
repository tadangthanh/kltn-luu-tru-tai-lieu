package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentVersionResponse extends BaseDto {

    private Long documentId;

    private int version;

    private String blobName;

    private String type; // docx, pdf...

    private long size;

    private LocalDateTime expiredAt;
}

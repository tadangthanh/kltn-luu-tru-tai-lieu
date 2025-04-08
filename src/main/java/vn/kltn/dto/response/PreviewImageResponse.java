package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class PreviewImageResponse extends BaseDto {
    private Long documentId;
    private String imageBlobName;
    private int pageNumber;
}

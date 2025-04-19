package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentResponse extends ResourceResponse {
    private String type;
    private String blobName;
    private int version;
}

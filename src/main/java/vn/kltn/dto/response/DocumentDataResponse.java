package vn.kltn.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class DocumentDataResponse implements Serializable {
    private Long documentId;
    private byte[] data;
    private String name;
    private String type;
}

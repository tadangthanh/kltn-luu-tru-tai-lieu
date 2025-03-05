package vn.kltn.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class FileDataResponse implements Serializable {
    private byte[] data;
    private String fileName;
    private String fileType;
}

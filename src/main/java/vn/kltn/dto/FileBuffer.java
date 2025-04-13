package vn.kltn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileBuffer {
    private String fileName;
    private byte[] data;
    private Long size;
    private String contentType;
}

package vn.kltn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadProgressDTO {
    private String fileName;
    private int totalFileUploaded;
    private int totalFile;
}

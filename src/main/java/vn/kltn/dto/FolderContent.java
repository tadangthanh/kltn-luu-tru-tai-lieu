package vn.kltn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FolderContent {
    private String path; // ví dụ: folderA/folderB/file.txt
    private boolean isFolder;
    private String blobName; // chỉ có nếu là file
}

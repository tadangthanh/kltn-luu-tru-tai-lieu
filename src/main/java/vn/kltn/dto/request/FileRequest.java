package vn.kltn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class FileRequest implements Serializable {
    private String description;
    private String fileName;
    private TagRequest[] tags;
    private boolean isPublic;
}

package vn.kltn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.kltn.index.DocumentIndex;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProcessUploadResult {
    private final boolean cancelled;
    private final List<String> blobNames;
    private final List<DocumentIndex> documentIndices;
}

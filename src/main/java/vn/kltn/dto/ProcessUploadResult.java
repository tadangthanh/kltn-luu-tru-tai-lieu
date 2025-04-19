package vn.kltn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.kltn.dto.response.DocumentResponse;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProcessUploadResult {
    private final boolean cancelled;
    private final List<DocumentResponse> documents;
}

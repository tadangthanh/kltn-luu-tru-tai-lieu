package vn.kltn.service;

import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.ProcessUploadResult;
import vn.kltn.dto.UploadContext;

import java.util.List;

public interface IUploadProcessor {
    List<String> process(List<FileBuffer> bufferedFiles);
    /**
     * Process file upload with cancellation support
     */
    ProcessUploadResult processUpload(UploadContext context, List<FileBuffer> files);

}

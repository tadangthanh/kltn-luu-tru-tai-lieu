package vn.kltn.service;

import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.UploadContext;

import java.util.List;

public interface IUploadProcessor {
    List<String> processUpload(UploadContext context, List<FileBuffer> files);

}

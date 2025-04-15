package vn.kltn.service;

import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;

import java.util.List;

public interface IUploadProcessor {
    List<String> process(CancellationToken token, List<FileBuffer> bufferedFiles);
}

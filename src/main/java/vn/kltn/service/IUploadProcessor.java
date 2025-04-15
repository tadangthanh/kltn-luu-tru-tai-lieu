package vn.kltn.service;

import vn.kltn.dto.FileBuffer;

import java.util.List;

public interface IUploadProcessor {
    List<String> process(List<FileBuffer> bufferedFiles);
}

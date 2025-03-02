package vn.kltn.service;

import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.dto.response.FileShareView;
import vn.kltn.entity.FileShare;

public interface IFileShareService {
    FileShareResponse createFileShareLink(Long fileId, FileShareRequest fileShareRequest);

    FileShareView viewFile(String token, String password);
    FileShare getShareFileByToken(String token);
}

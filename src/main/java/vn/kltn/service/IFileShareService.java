package vn.kltn.service;

import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.dto.response.FileShareView;

import java.io.InputStream;

public interface IFileShareService {
    FileShareResponse shareFile(FileShareRequest fileShareRequest);

    FileShareView viewFile(String token, String password);
}

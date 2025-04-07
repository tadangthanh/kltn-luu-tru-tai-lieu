package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface IDocumentConversionService {

    String convertFile(MultipartFile file, String targetFormat);

    void deleteFileIfExists(File file);
}

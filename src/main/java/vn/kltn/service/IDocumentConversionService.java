package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface IDocumentConversionService {

    String convertFile(MultipartFile file, String targetFormat);

    String convertStoredFile(String blobName, String targetFormat);

    List<String> convertPdfToImagesAndUpload(String blobName,String pages);

    void deleteFileIfExists(File file);
}

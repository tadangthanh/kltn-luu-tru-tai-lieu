package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.entity.Document;

import java.io.File;
import java.util.List;

public interface IDocumentConversionService {

    String convertFile(MultipartFile file, String targetFormat);

    String convertStoredFile(String blobName, String targetFormat);

    List<String> convertPdfToImagesAndUpload(Document document, List<Integer> pages);

    File convertToPdf(File file);

    void deleteFileIfExists(File file);

    File downloadAsPdf(String blobName);
}

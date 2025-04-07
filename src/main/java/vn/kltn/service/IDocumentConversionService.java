package vn.kltn.service;

import java.io.File;
import java.io.IOException;

public interface IDocumentConversionService {

    File convertWordToPdf(File wordFile) throws IOException;

    byte[] readPdfFileAsBytes(File pdfFile) throws IOException;

    void deleteFileIfExists(File file);
}

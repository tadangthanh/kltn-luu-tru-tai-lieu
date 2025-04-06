package vn.kltn.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IDocumentConversionService {
    byte[] convertWordToPdf(InputStream inputStream);

    File convertWordToPdf(File wordFile) throws IOException;

    public byte[] readPdfFileAsBytes(File pdfFile) throws IOException;

    void deleteFileIfExists(File file);
}

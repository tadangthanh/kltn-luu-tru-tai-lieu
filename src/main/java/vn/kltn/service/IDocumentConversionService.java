package vn.kltn.service;

import java.io.InputStream;

public interface IDocumentConversionService {
    byte[] convertWordToPdf(InputStream inputStream);
}

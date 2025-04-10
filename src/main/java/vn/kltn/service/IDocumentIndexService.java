package vn.kltn.service;

import vn.kltn.entity.Document;

import java.io.InputStream;

public interface IDocumentIndexService {
    void indexDocument(Document document, InputStream inputStream);
}

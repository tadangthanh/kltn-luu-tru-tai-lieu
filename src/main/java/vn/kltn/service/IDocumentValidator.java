package vn.kltn.service;

import vn.kltn.entity.Document;

public interface IDocumentValidator {
    void validateDocumentDeleted(Document document);
    void validateDocumentNotDeleted(Document document);
}

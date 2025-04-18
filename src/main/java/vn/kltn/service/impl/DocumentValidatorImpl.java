package vn.kltn.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Document;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.service.IDocumentValidator;
@Component
@Slf4j(topic = "DOCUMENT_VALIDATOR")
public class DocumentValidatorImpl implements IDocumentValidator {
    @Override
    public void validateDocumentDeleted(Document document) {
        if(document.getDeletedAt() == null) {
            log.warn("Document with id {} is not deleted", document.getId());
            throw new ConflictResourceException("Tài liệu chưa bị xóa");
        }
    }

    @Override
    public void validateDocumentNotDeleted(Document document) {
        if(document.getDeletedAt() != null) {
            log.warn("Document with id {} is deleted", document.getId());
            throw new ConflictResourceException("Tài liệu đã bị xóa");
        }
    }
}

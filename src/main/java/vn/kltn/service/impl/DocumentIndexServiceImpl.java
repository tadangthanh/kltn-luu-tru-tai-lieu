package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.Tag;
import vn.kltn.exception.CustomIOException;
import vn.kltn.index.DocumentSegmentEntity;
import vn.kltn.map.DocumentSegmentMapper;
import vn.kltn.repository.elasticsearch.CustomDocumentSegmentRepo;
import vn.kltn.repository.elasticsearch.DocumentSegmentRepo;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.IDocumentIndexService;
import vn.kltn.service.IDocumentPermissionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_INDEX_SERVICE")
public class DocumentIndexServiceImpl implements IDocumentIndexService {
    private final DocumentSegmentRepo documentSegmentRepo;
    private final DocumentSegmentMapper documentSegmentMapper;
    private final IDocumentHasTagService documentHasTagService;
    private final IDocumentPermissionService documentPermissionService;
    private final CustomDocumentSegmentRepo customDocumentSegmentRepo;
    @Override
    @Async
    public void indexDocument(Document document, InputStream inputStream) {
        List<String> segments = new ArrayList<>();
        // Phân loại theo type file (ở đây ví dụ là PDF)
        if (document.getType().equalsIgnoreCase("application/pdf")) {
            segments = extractPdfByPage(inputStream);
        } else if (document.getType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            segments = extractDocxByChunk(inputStream, 200); // 200 từ mỗi đoạn
        }

        int segmentNumber = 0;
        List<DocumentSegmentEntity> segmentEntities = new ArrayList<>();
        List<String> tagsList = getTagsByDocumentId(document.getId());
        List<Long> sharedWith = getSharedWithByDocumentId(document.getId());
        for (String segment : segments) {
            DocumentSegmentEntity segmentEntity = documentSegmentMapper.toSegmentEntity(document);
            segmentEntity.setId(UUID.randomUUID().toString());
            segmentEntity.setContent(segment);
            segmentEntity.setSegmentNumber(segmentNumber++);
            segmentEntity.setTags(tagsList);
            segmentEntity.setSharedWith(sharedWith);
            segmentEntities.add(segmentEntity);
        }
        documentSegmentRepo.saveAll(segmentEntities); // index hàng loạt
        log.info("Indexed {} segments for document {}", segmentNumber, document.getId());
    }

    @Override
    public void deleteIndexByDocumentId(Long documentId) {
        documentSegmentRepo.deleteByDocumentId(documentId);
    }

    @Override
    public void markDeleteByDocumentIds(List<Long> documentIds) {
        customDocumentSegmentRepo.markDeleteByDocumentIds(documentIds);
    }

    @Override
    public void deleteIndexByDocumentIds(List<Long> documentIds) {
        documentSegmentRepo.deleteAllByDocumentIdIn(documentIds);
    }

    @Override
    public void markDeleteDocument(Long documentId) {
        log.info("mark deleted documentId: {}", documentId);
        customDocumentSegmentRepo.markDeletedByDocumentId(documentId);
    }

    private List<String> getTagsByDocumentId(Long documentId) {
        return documentHasTagService.getTagsByDocumentId(documentId).stream().map(Tag::getName).toList();
    }


    private List<Long> getSharedWithByDocumentId(Long documentId) {
        return documentPermissionService.getSharedWithByDocumentId(documentId).stream().toList();
    }

    private List<String> extractDocxByChunk(InputStream inputStream, int wordsPerChunk) {
        List<String> chunks = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder currentChunk = new StringBuilder();
            int currentWordCount = 0;

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String[] words = paragraph.getText().split("\\s+");
                for (String word : words) {
                    currentChunk.append(word).append(" ");
                    currentWordCount++;
                    if (currentWordCount >= wordsPerChunk) {
                        chunks.add(currentChunk.toString().trim());
                        currentChunk.setLength(0);
                        currentWordCount = 0;
                    }
                }
            }
            if (!currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());
            }
        } catch (IOException e) {
            log.error("Error chunking DOCX file: {}", e.getMessage());
            throw new CustomIOException("Error chunking DOCX file");
        }

        return chunks;
    }

    private List<String> extractPdfByPage(InputStream inputStream) {
        List<String> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = document.getNumberOfPages();

            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                pages.add(pageText.trim());
            }
        } catch (IOException e) {
            log.error("Error chunking PDF file: {}", e.getMessage());
            throw new CustomIOException("Error chunking PDF file");
        }

        return pages;
    }

}

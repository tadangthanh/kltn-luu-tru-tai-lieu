package vn.kltn.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IDocumentConversionService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_SERVICE")
public class DocumentConversionServiceImpl implements IDocumentConversionService {

    @Override
    public byte[] convertWordToPdf(InputStream inputStream) {
        try {
            // Đọc tệp Word
            XWPFDocument wordDoc = new XWPFDocument(inputStream);
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

            // Tạo tài liệu PDF
            Document pdfDoc = new Document();
            PdfWriter.getInstance(pdfDoc, pdfOutputStream);
            pdfDoc.open();

            // Chuyển từng đoạn văn bản từ Word sang PDF
            for (XWPFParagraph paragraph : wordDoc.getParagraphs()) {
                pdfDoc.add(new Paragraph(paragraph.getText()));
            }

            // Đóng tài liệu
            pdfDoc.close();
            wordDoc.close();
            return pdfOutputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }


}

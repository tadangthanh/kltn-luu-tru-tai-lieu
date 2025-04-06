package vn.kltn.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.exception.BadRequestException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IDocumentConversionService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_SERVICE")
public class DocumentConversionServiceImpl implements IDocumentConversionService {
    @Value("${app.conversion.temp-dir}")
    private String tempDir;

    // Hàm chuyển đổi file Word thành PDF
    public File convertWordToPdf(File wordFile) throws IOException {
        String outputFilePath = wordFile.getParent() + File.separator + wordFile.getName().replace(".docx", ".pdf");
        File pdfFile = new File(outputFilePath);

        // Gọi lệnh LibreOffice với đường dẫn đầy đủ
        ProcessBuilder processBuilder = new ProcessBuilder(
                "soffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", wordFile.getParent(),
                wordFile.getAbsolutePath()
        );

        Process process = processBuilder.start();
        try {
            int exitValue = process.waitFor(); // Chờ quá trình hoàn tất
            if (exitValue != 0) {
                throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        }

        if (!pdfFile.exists()) {
            log.error("File PDF không được tạo ra.");
            throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        }

        return pdfFile;
    }

    // Đọc nội dung file PDF và trả về dưới dạng byte array
    @Override
    public byte[] readPdfFileAsBytes(File pdfFile) throws IOException {
        return Files.readAllBytes(pdfFile.toPath());
    }

    // Xóa file tạm sau khi xử lý xong
    @Override
    public void deleteFileIfExists(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

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

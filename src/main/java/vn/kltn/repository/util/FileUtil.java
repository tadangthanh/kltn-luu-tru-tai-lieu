package vn.kltn.repository.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.FileBuffer;
import vn.kltn.exception.CustomIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "FILE_UTIL")
public class FileUtil {
    // Add file utility methods here
    // For example, methods to read/write files, check file types, etc.

    // Example method
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return ""; // No extension found
        }
        return fileName.substring(lastIndexOfDot + 1);
    }


    public static String extractDocxText(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder fullText = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                fullText.append(paragraph.getText()).append("\n");
            }

            return fullText.toString().trim();
        } catch (IOException e) {
            log.error("Error extracting DOCX text: {}", e.getMessage());
            throw new CustomIOException("Error extracting DOCX file");
        }
    }

    public static String extractPdfText(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        } catch (IOException e) {
            log.error("Error extracting PDF text: {}", e.getMessage());
            throw new CustomIOException("Error extracting PDF file");
        }
    }

    public static String extractExcelText(InputStream inputStream) {
        StringBuilder text = new StringBuilder();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                text.append(cell.getStringCellValue()).append(" ");
                                break;
                            case NUMERIC:
                                text.append(cell.getNumericCellValue()).append(" ");
                                break;
                            case BOOLEAN:
                                text.append(cell.getBooleanCellValue()).append(" ");
                                break;
                            default:
                                break;
                        }
                    }
                    text.append("\n");
                }
            }
        } catch (IOException e) {
            log.error("Error extracting Excel text: {}", e.getMessage());
            throw new CustomIOException("Error extracting Excel file");
        }

        return text.toString().trim();
    }

    public static String extractPptxText(InputStream inputStream) {
        StringBuilder text = new StringBuilder();

        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error extracting PPTX text: {}", e.getMessage());
            throw new CustomIOException("Error extracting PowerPoint file");
        }

        return text.toString().trim();
    }

    public static String extractTxtText(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            return text.toString().trim();
        } catch (IOException e) {
            log.error("Error extracting TXT text: {}", e.getMessage());
            throw new CustomIOException("Error extracting TXT file");
        }
    }

    public static String extractTextByType(String mimeType, InputStream inputStream) {
        return switch (mimeType) {
            case "application/pdf" -> extractPdfText(inputStream);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> // .docx
                    extractDocxText(inputStream);
            case "application/msword" -> // .doc
                    extractDocText(inputStream);
            case "text/plain" -> // .txt
                    extractTxtText(inputStream);
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> // .xlsx
                    extractExcelText(inputStream);
            case "application/vnd.ms-excel" -> // .xls
                    extractXlsText(inputStream);
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> // .pptx
                    extractPptxText(inputStream);
            case "application/vnd.ms-powerpoint" -> // .ppt
                    extractPptText(inputStream);
            default -> {
                log.error("Unsupported file type: {}", mimeType);
                throw new CustomIOException("Unsupported file type: " + mimeType);
            }
        };
    }


    public static String extractXlsText(InputStream inputStream) {
        StringBuilder text = new StringBuilder();
        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING -> text.append(cell.getStringCellValue()).append(" ");
                            case NUMERIC -> text.append(cell.getNumericCellValue()).append(" ");
                            case BOOLEAN -> text.append(cell.getBooleanCellValue()).append(" ");
                            default -> {
                            }
                        }
                    }
                    text.append("\n");
                }
            }
        } catch (IOException e) {
            log.error("Error extracting XLS text: {}", e.getMessage());
            throw new CustomIOException("Error extracting XLS file");
        }
        return text.toString().trim();
    }

    public static String extractPptText(InputStream inputStream) {
        StringBuilder text = new StringBuilder();
        try (HSLFSlideShow ppt = new HSLFSlideShow(inputStream)) {
            for (HSLFSlide slide : ppt.getSlides()) {
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape textShape) {
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error extracting PPT text: {}", e.getMessage());
            throw new CustomIOException("Error extracting PPT file");
        }
        return text.toString().trim();
    }

    public static String extractDocText(InputStream inputStream) {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText().trim();
        } catch (IOException e) {
            log.error("Error extracting DOC text: {}", e.getMessage());
            throw new CustomIOException("Error extracting DOC file");
        }
    }

    public static List<FileBuffer> getFileBufferList(MultipartFile[] files) {
        List<FileBuffer> list = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                list.add(new FileBuffer(file.getOriginalFilename(), file.getBytes(), file.getSize(), file.getContentType()));
            } catch (IOException e) {
                throw new CustomIOException("Không đọc được file");
            }
        }
        return list;
    }
}

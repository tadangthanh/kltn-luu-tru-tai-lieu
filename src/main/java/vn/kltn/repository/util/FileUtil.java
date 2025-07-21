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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.FileMagic;
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
import vn.kltn.exception.ResourceNotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j(topic = "FILE_UTIL")
public class FileUtil {

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int idx = fileName.lastIndexOf('.');
        return (idx == -1) ? "" : fileName.substring(idx + 1).toLowerCase();
    }

    /**
     * Extract text based on MIME type and file extension, with fallback for mislabelled files.
     */
    public static String extractTextByType(String mimeType, String fileName, InputStream is) {
        try {
            byte[] data = is.readAllBytes();
            String ext = getFileExtension(fileName);

            // Try by MIME type first
            switch (mimeType) {
                case "application/msword":
                    return extractDocWithFallback(data, ext);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    return extractDocxText(new ByteArrayInputStream(data));
                case "application/pdf":
                    return extractPdfText(new ByteArrayInputStream(data));
                case "text/plain":
                    return extractTxtText(new ByteArrayInputStream(data));
                case "application/vnd.ms-excel":
                    return extractXlsText(new ByteArrayInputStream(data));
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                    return extractExcelText(new ByteArrayInputStream(data));
                case "application/vnd.ms-powerpoint":
                    return extractPptText(new ByteArrayInputStream(data));
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                    return extractPptxText(new ByteArrayInputStream(data));
                default:
                    // Fallback to extension-based detection
                    return extractByExtension(ext, new ByteArrayInputStream(data));
            }
        } catch (IOException e) {
            log.error("Error reading file bytes: {}", e.getMessage(), e);
            throw new CustomIOException("Error processing file");
        }
    }

    private static String extractDocWithFallback(byte[] data, String ext) {
        // Try old POIFS (.doc)
        try (InputStream is1 = new ByteArrayInputStream(data)) {
            return extractDocText(is1);
        } catch (IllegalArgumentException | IOException e) {
            log.warn("POIFS (.doc) extractor failed, trying OOXML (.docx): {}", e.getMessage());
        }
        // Try OOXML .docx
        try (InputStream is2 = new ByteArrayInputStream(data)) {
            return extractDocxText(is2);
        } catch (IOException e) {
            log.error("Error extracting DOCX text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting Word file");
        }
    }

    private static String extractByExtension(String ext, InputStream is) {
        switch (ext) {
            case "doc": return extractDocText(is);
            case "docx": return extractDocxText(is);
            case "xls": return extractXlsText(is);
            case "xlsx": return extractExcelText(is);
            case "ppt": return extractPptText(is);
            case "pptx": return extractPptxText(is);
            case "pdf": return extractPdfText(is);
            case "txt": return extractTxtText(is);
            default: throw new CustomIOException("Unsupported file extension: " + ext);
        }
    }

    public static String extractDocText(InputStream inputStream) {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText().trim();
        } catch (IOException e) {
            log.error("Error extracting DOC text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting DOC file");
        }
    }

    public static String extractDocxText(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(OPCPackage.open(inputStream))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : document.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString().trim();
        } catch (IOException | InvalidFormatException e) {
            log.error("Error extracting DOCX text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting DOCX file");
        }
    }

    public static String extractPdfText(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            return new PDFTextStripper().getText(document).trim();
        } catch (IOException e) {
            log.error("Error extracting PDF text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting PDF file");
        }
    }

    public static String extractTxtText(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        } catch (IOException e) {
            log.error("Error extracting TXT text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting TXT file");
        }
    }

    public static String extractExcelText(InputStream inputStream) {
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            return extractSheetText(wb);
        } catch (IOException e) {
            log.error("Error extracting XLSX text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting XLSX file");
        }
    }

    public static String extractXlsText(InputStream inputStream) {
        try (Workbook wb = new HSSFWorkbook(inputStream)) {
            return extractSheetText(wb);
        } catch (IOException e) {
            log.error("Error extracting XLS text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting XLS file");
        }
    }

    private static String extractSheetText(Workbook wb) {
        StringBuilder sb = new StringBuilder();
        for (Sheet sheet : wb) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING -> sb.append(cell.getStringCellValue()).append(" ");
                        case NUMERIC -> sb.append(cell.getNumericCellValue()).append(" ");
                        case BOOLEAN -> sb.append(cell.getBooleanCellValue()).append(" ");
                        default -> {}
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    public static String extractPptxText(InputStream inputStream) {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape ts) {
                        sb.append(ts.getText()).append("\n");
                    }
                }
            }
            return sb.toString().trim();
        } catch (IOException e) {
            log.error("Error extracting PPTX text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting PPTX file");
        }
    }

    public static String extractPptText(InputStream inputStream) {
        try (HSLFSlideShow ppt = new HSLFSlideShow(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (HSLFSlide slide : ppt.getSlides()) {
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape ts) {
                        sb.append(ts.getText()).append("\n");
                    }
                }
            }
            return sb.toString().trim();
        } catch (IOException e) {
            log.error("Error extracting PPT text: {}", e.getMessage(), e);
            throw new CustomIOException("Error extracting PPT file");
        }
    }

    public static List<FileBuffer> getFileBufferList(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new ResourceNotFoundException("Không có file nào được gửi lên");
        }
        Set<String> allowed = Set.of("pdf","doc","docx","xls","xlsx","ppt","pptx","txt");
        List<FileBuffer> list = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile f = files[i];
            if (f.isEmpty()) throw new ResourceNotFoundException("File " + (i+1) + " bị trống");
            String name = f.getOriginalFilename();
            if (name == null || !name.contains(".")) continue;
            String ext = getFileExtension(name);
            if (!allowed.contains(ext)) continue;
            try {
                list.add(new FileBuffer(name, f.getBytes(), f.getSize(), f.getContentType()));
            } catch (IOException e) {
                throw new CustomIOException("Không đọc được file");
            }
        }
        if (list.isEmpty()) throw new ResourceNotFoundException("Không có file hợp lệ nào được gửi lên");
        return list;
    }

    public static String generateFileName(String blobName) {
        int idx = blobName.lastIndexOf('_');
        return (idx == -1) ? blobName : blobName.substring(idx + 1);
    }

    public static String getOriginalFileName(String blobName) {
        int first = blobName.indexOf('_');
        int last = blobName.lastIndexOf('_');
        if (first < 0 || last <= first) return blobName;
        return blobName.substring(first + 1, last);
    }
}
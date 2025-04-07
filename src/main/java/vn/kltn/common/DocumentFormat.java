package vn.kltn.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
public enum DocumentFormat {
    DOCX("docx"),
    DOC("doc"),
    PDF("pdf"),
    PPTX("pptx"),
    PPT("ppt"),
    XLSX("xlsx"),
    XLS("xls"),
    ODT("odt"),
    ODP("odp"),
    ODS("ods"),
    TXT("txt");

    private final String extension;

    DocumentFormat(String extension) {
        this.extension = extension;
    }

    public static Optional<DocumentFormat> fromExtension(String ext) {
        return Arrays.stream(values())
                .filter(f -> f.getExtension().equalsIgnoreCase(ext))
                .findFirst();
    }
    public static final Map<DocumentFormat, Set<DocumentFormat>> SUPPORTED_CONVERSIONS = Map.of(
            DocumentFormat.DOCX, Set.of(DocumentFormat.PDF, DocumentFormat.ODT, DocumentFormat.TXT),
            DocumentFormat.DOC, Set.of(DocumentFormat.PDF, DocumentFormat.ODT),
            DocumentFormat.PPTX, Set.of(DocumentFormat.PDF, DocumentFormat.ODP),
            DocumentFormat.XLSX, Set.of(DocumentFormat.PDF, DocumentFormat.ODS),
            DocumentFormat.ODT, Set.of(DocumentFormat.PDF),
            DocumentFormat.PDF, Set.of(DocumentFormat.DOCX, DocumentFormat.ODT), // nếu hỗ trợ
            DocumentFormat.TXT, Set.of(DocumentFormat.PDF)
    );

}

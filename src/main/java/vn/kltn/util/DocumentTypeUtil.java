package vn.kltn.util;

public class DocumentTypeUtil {

    // Hàm trả về thông tin fileType và documentType dựa trên phần mở rộng
    public static DocumentTypeInfo getDocumentTypeInfo(String fileExtension) {
        DocumentTypeInfo documentTypeInfo = new DocumentTypeInfo();

        switch (fileExtension.toLowerCase()) {
            case "docx":
            case "doc":
                documentTypeInfo.setFileType("docx");
                documentTypeInfo.setDocumentType("word");
                break;
            case "pdf":
                documentTypeInfo.setFileType("pdf");
                documentTypeInfo.setDocumentType("pdf");
                break;
            case "xlsx":
            case "xls":
                documentTypeInfo.setFileType("xlsx");
                documentTypeInfo.setDocumentType("spreadsheet");
                break;
            case "pptx":
            case "ppt":
                documentTypeInfo.setFileType("pptx");
                documentTypeInfo.setDocumentType("presentation");
                break;
            case "txt":
                documentTypeInfo.setFileType("txt");
                documentTypeInfo.setDocumentType("text");
                break;
            case "html":
                documentTypeInfo.setFileType("html");
                documentTypeInfo.setDocumentType("html");
                break;
            default:
                documentTypeInfo.setFileType("unknown");
                documentTypeInfo.setDocumentType("unknown");
                break;
        }

        return documentTypeInfo;
    }

    // Lớp giúp lưu thông tin về fileType và documentType
    public static class DocumentTypeInfo {
        private String fileType;
        private String documentType;

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }
    }
}

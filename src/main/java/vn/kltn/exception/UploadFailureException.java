package vn.kltn.exception;

public class UploadFailureException extends RuntimeException {
    public UploadFailureException(String message) {
        super(message);
    }
}

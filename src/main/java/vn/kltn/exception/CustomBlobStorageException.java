package vn.kltn.exception;

public class CustomBlobStorageException extends RuntimeException {
    public CustomBlobStorageException(String message) {
        super(message);
    }
}

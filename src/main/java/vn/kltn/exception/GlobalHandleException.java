package vn.kltn.exception;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
public class GlobalHandleException extends ResponseEntityExceptionHandler {
    @ExceptionHandler({ResourceNotFoundException.class})
    public final ResponseEntity<ErrorResponse> handleResourceNotFound(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }
    @ExceptionHandler({AccessDeniedException.class,AccessDeniedException.class})
    public final ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
        errorResponse.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }
    @ExceptionHandler({UnauthorizedException.class,InvalidTokenException.class})
    public final ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }
    @ExceptionHandler({BadRequestException.class,UploadFailureException.class,InvalidDataException.class,PasswordMismatchException.class})
    public final ResponseEntity<ErrorResponse> handleUploadFail(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }
    @ExceptionHandler({CustomBlobStorageException.class})
    public final ResponseEntity<ErrorResponse> handleBlobStorageException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }
    @ExceptionHandler({ConflictResourceException.class})
    public final ResponseEntity<ErrorResponse> handleConflict(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        errorResponse.setError(HttpStatus.CONFLICT.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    // method override return field and details
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull  HttpStatusCode status, @NonNull  WebRequest request) {
        ErrorObjectDetails errorObjectDetails = new ErrorObjectDetails();
        errorObjectDetails.setTimestamp(LocalDateTime.now());
        errorObjectDetails.setMessage(ex.getMessage().substring(ex.getMessage().lastIndexOf("[")+1,ex.getMessage().lastIndexOf("]")-1));
        errorObjectDetails.setField(Objects.requireNonNull(ex.getBindingResult().getFieldError()).getField());
        errorObjectDetails.setDetails(ex.getBindingResult().getFieldError().getDefaultMessage());
        return new ResponseEntity<>(errorObjectDetails, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,@NonNull HttpHeaders headers,@NonNull HttpStatusCode status,@NonNull WebRequest request) {
        ErrorObjectDetails errorObjectDetails = new ErrorObjectDetails();
        errorObjectDetails.setTimestamp(LocalDateTime.now());
        errorObjectDetails.setField("Request body");
        errorObjectDetails.setMessage("Cannot deserialize value");
        errorObjectDetails.setDetails("Request body is not valid");
        return new ResponseEntity<>(errorObjectDetails, HttpStatus.BAD_REQUEST);
    }
}

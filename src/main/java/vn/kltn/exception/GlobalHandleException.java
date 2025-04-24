package vn.kltn.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Date;
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


    @ExceptionHandler({UnauthorizedException.class, InvalidTokenException.class, AccessDeniedException.class})
    public final ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    @ExceptionHandler({BadRequestException.class, DuplicateResourceException.class, UploadFailureException.class, InvalidDataException.class, PasswordMismatchException.class, PropertyReferenceException.class})
    public final ResponseEntity<ErrorResponse> handleBadRequestException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    @ExceptionHandler({CustomBlobStorageException.class, CustomIOException.class,AuthVerifyException.class, ConversionException.class,InsertIndexException.class})
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

    @ExceptionHandler({UnsupportedFileFormatException.class})
    public final ResponseEntity<ErrorResponse> handleUnsupported(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        errorResponse.setError(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    // method override return field and details
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        ErrorObjectDetails errorObjectDetails = new ErrorObjectDetails();
        errorObjectDetails.setTimestamp(LocalDateTime.now());
        errorObjectDetails.setMessage(ex.getMessage().substring(ex.getMessage().lastIndexOf("[") + 1, ex.getMessage().lastIndexOf("]") - 1));
        errorObjectDetails.setField(Objects.requireNonNull(ex.getBindingResult().getFieldError()).getField());
        errorObjectDetails.setDetails(ex.getBindingResult().getFieldError().getDefaultMessage());
        return new ResponseEntity<>(errorObjectDetails, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        ErrorObjectDetails errorObjectDetails = new ErrorObjectDetails();
        errorObjectDetails.setTimestamp(LocalDateTime.now());
        errorObjectDetails.setField("Request body");
        errorObjectDetails.setMessage("Cannot deserialize value");
        errorObjectDetails.setDetails("Request body is not valid");
        return new ResponseEntity<>(errorObjectDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(LocalDateTime.now());
        String message = e.getMessage();
        if (e instanceof ConstraintViolationException) {
            message = message.substring(message.indexOf(" ") + 1);
            errorResponse.setError("PathVariable validation error");
        }
        errorResponse.setMessage(message.trim());

        return errorResponse;
    }
}

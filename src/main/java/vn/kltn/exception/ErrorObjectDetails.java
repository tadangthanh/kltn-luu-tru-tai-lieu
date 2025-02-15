package vn.kltn.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorObjectDetails {
    private String message;
    private LocalDateTime timestamp;
    private String field;
    private String details;
}

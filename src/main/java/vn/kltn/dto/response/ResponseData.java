package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ResponseData<T> {
    private final int status;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // put,patch,delete
    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // get,post
    public ResponseData(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

}


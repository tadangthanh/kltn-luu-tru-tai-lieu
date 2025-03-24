package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserResponse implements Serializable {
    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;
}

package vn.kltn.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeysResponse {
    private String publicKey;
    private String privateKey;
}

package vn.kltn.service;

import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;

public interface IAuthenticationService {
    TokenResponse login(AuthRequest authRequest);
    TokenResponse refreshToken(String refreshToken);
}

package vn.kltn.service;

import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;

public interface IAuthenticationService {
    TokenResponse getAccessToken(AuthRequest authRequest);

    TokenResponse getRefreshToken(String token);




}

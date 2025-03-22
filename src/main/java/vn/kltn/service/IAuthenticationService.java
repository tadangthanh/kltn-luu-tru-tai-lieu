package vn.kltn.service;

import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.entity.Role;
import vn.kltn.entity.User;

import java.util.Set;

public interface IAuthenticationService {
    TokenResponse getAccessToken(AuthRequest authRequest);

    TokenResponse getRefreshToken(String token);

    User getAuthUser();
}

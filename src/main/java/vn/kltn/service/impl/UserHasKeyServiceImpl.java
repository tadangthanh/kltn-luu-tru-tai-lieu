package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.User;
import vn.kltn.entity.UserHasKey;
import vn.kltn.repository.UserHasKeyRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IUserHasKeyService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "USER_HAS_KEY_SERVICE")
public class UserHasKeyServiceImpl implements IUserHasKeyService {
    private final UserHasKeyRepo userHasKeyRepo;
    private final IAuthenticationService authenticationService;

    @Override
    public UserHasKey savePublicKey(User user, String publicKey) {
        UserHasKey userHasKey = new UserHasKey();
        userHasKey.setPublicKey(publicKey);
        userHasKey.setUser(user);
        userHasKey.setActive(true);
        userHasKeyRepo.disableAllKeyByUserId(user.getId());
        userHasKeyRepo.save(userHasKey);
        return userHasKey;
    }

    @Override
    public String getPublicKeyActiveByUserAuth() {
        User authUser = authenticationService.getCurrentUser();
        return userHasKeyRepo.getPublicKeyActiveByUserId(authUser.getId());
    }
}

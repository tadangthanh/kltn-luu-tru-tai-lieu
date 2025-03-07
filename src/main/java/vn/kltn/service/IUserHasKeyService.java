package vn.kltn.service;

import vn.kltn.entity.User;
import vn.kltn.entity.UserHasKey;

public interface IUserHasKeyService {
    UserHasKey savePublicKey(User user,String publicKey);
    String getPublicKeyActiveByUserAuth();
}

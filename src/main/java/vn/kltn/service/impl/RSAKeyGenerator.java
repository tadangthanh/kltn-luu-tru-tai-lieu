package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.KeysResponse;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IKeyGenerator;
import vn.kltn.service.IUserHasKeyService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@Slf4j(topic = "RSA_KEY_GENERATOR")
@RequiredArgsConstructor
@Transactional
public class RSAKeyGenerator implements IKeyGenerator {
    private final IAuthenticationService authenticationService;
    private final IUserHasKeyService userHasKeyService;

    @Override
    public KeysResponse generatePublicAndPrivateKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            // 2. Chuyển đổi khóa riêng thành định dạng PEM
            String privateKeyPEM = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            // 3. Chuyển đổi khóa công khai thành định dạng PEM
            String publicKeyPEM = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            return KeysResponse.builder().privateKey(privateKeyPEM).publicKey(publicKeyPEM).build();
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    @Override
    public byte[] getPrivateKey() {
        KeysResponse keysResponse = generatePublicAndPrivateKey();
        User authUser = authenticationService.getCurrentUser();
        userHasKeyService.savePublicKey(authUser, keysResponse.getPublicKey());
        return keysResponse.getPrivateKey().getBytes();
    }

}

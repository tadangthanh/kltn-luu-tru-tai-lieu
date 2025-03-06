package vn.kltn.service;

import vn.kltn.dto.response.KeysResponse;

public interface IKeyGenerator {
    KeysResponse generatePublicAndPrivateKey();
    byte[] getPrivateKey();
}

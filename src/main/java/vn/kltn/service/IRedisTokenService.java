package vn.kltn.service;

import vn.kltn.entity.RedisToken;

public interface IRedisTokenService {
    String save(RedisToken redisToken);
    void delete(String id);
    boolean isExists(String id);
}

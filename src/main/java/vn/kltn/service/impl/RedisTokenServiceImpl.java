package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.kltn.entity.RedisToken;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.RedisTokenRepo;
import vn.kltn.service.IRedisTokenService;

@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements IRedisTokenService {

    private final RedisTokenRepo redisTokenRepo;

    @Override
    public String save(RedisToken redisToken) {
        RedisToken result= redisTokenRepo.save(redisToken);
        return result.getId();
    }

    @Override
    public void delete(String id) {
        redisTokenRepo.deleteById(id);
    }

    @Override
    public boolean isExists(String id) {
        if (!redisTokenRepo.existsById(id)) {
            throw new InvalidDataException("Token không tồn tại!");
        }
        return true;
    }
}

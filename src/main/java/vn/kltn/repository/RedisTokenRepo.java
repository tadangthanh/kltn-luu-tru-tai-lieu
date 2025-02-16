package vn.kltn.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.RedisToken;

@Repository
public interface RedisTokenRepo extends CrudRepository<RedisToken,String> {
}

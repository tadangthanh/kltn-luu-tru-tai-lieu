package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.UserIndexResponse;
import vn.kltn.entity.User;
import vn.kltn.index.UserIndex;

import java.util.List;

public interface IUserIndexService {
    UserIndex addUser(User user);

    PageResponse<List<UserIndexResponse>> searchUsersByEmail(String email, Pageable pageable);
}

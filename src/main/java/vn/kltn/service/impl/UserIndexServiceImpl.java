package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.UserIndexResponse;
import vn.kltn.entity.User;
import vn.kltn.index.UserIndex;
import vn.kltn.map.UserMapper;
import vn.kltn.repository.UserRepo;
import vn.kltn.repository.elasticsearch.CustomUserIndexRepo;
import vn.kltn.repository.elasticsearch.UserIndexRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IUserIndexService;

import java.util.List;

@Service
@Slf4j(topic = "USER_INDEX_SERVICE")
@RequiredArgsConstructor
public class UserIndexServiceImpl implements IUserIndexService {
    private final UserIndexRepo userIndexRepo;
    private final UserMapper userMapper;
    private final CustomUserIndexRepo customUserIndexRepo;

    @Override
    public UserIndex addUser(User user) {
        // Kiểm tra xem người dùng đã tồn tại trong Elasticsearch hay chưa
        if (userIndexRepo.existsById(user.getId().toString())) {
            return userIndexRepo.findById(user.getId().toString()).orElse(null);
        }
        // Nếu không tồn tại, thêm người dùng vào Elasticsearch
        UserIndex userIndex = userMapper.toUserIndex(user);
        return userIndexRepo.save(userIndex);
    }

    @Override
    public PageResponse<List<UserIndexResponse>> searchUsersByEmail(String email, Pageable pageable) {
        if(email == null || email.isEmpty()) {
            email="";
        }
        // Tạo Pageable object từ thông tin phân trang

        // Sử dụng repository để thực hiện tìm kiếm
//        Page<UserIndex> userPage = userIndexRepo.findByEmailContaining(email, pageable);

        // Chuyển đổi kết quả tìm kiếm thành PageResponse
        return customUserIndexRepo.search(email, pageable);
    }
}

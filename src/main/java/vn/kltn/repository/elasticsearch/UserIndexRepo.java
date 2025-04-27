package vn.kltn.repository.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.UserIndex;

public interface UserIndexRepo extends ElasticsearchRepository<UserIndex, String> {
    // Tìm kiếm theo email, hỗ trợ phân trang
    Page<UserIndex> findByEmailContaining(String email, Pageable pageable);
}

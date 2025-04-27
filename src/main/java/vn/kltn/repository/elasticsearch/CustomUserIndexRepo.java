package vn.kltn.repository.elasticsearch;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.UserIndexResponse;

import java.util.List;
import java.util.Set;

public interface CustomUserIndexRepo {
    PageResponse<List<UserIndexResponse>> search(String query, Pageable pageable);
}

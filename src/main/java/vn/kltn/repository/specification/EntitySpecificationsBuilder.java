package vn.kltn.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static vn.kltn.repository.specification.SearchOperation.*;

public class EntitySpecificationsBuilder<T> {
    public final List<SpecSearchCriteria> params;

    public EntitySpecificationsBuilder() {
        this.params = new ArrayList<>();
    }

    // API
    public EntitySpecificationsBuilder<T> with(final String key, final String operation, final Object value, final String prefix, final String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public EntitySpecificationsBuilder<T> with(final String orPredicate, final String key, final String operation, final Object value, final String prefix, final String suffix) {
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOperation != null) {
            if (searchOperation == EQUALITY) { // the operation may be complex operation
                // bắt đầu bằng dấu *
                final boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
                // kết thúc bằng dấu *
                final boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);
                // thì tìm kiếm theo kiểu "tồn tại"
                if (startWithAsterisk && endWithAsterisk) {
                    searchOperation = CONTAINS;
                } else if (startWithAsterisk) {
                    searchOperation = ENDS_WITH;    // tìm kiếm kết thúc bằng kí tự chỉ định
                } else if (endWithAsterisk) {
                    searchOperation = STARTS_WITH; // tìm kiếm bắt đầu bằng kí tự chỉ định
                }
            }
            params.add(new SpecSearchCriteria(orPredicate, key, searchOperation, value));
        }
        return this;
    }

    // hàm này trả về một đối tượng Specification<SubTopic> dựa trên các tiêu chí tìm kiếm đã được xây dựng từ method with
    public Specification<T> build() {
        if (params.isEmpty()) return null;
        Specification<T> result = new EntitySpecification<>(params.get(0));
        for (int i = 1; i < params.size(); i++) {
            // các tiêu chí tìm kiếm được xây dựng từ method with sẽ được kết hợp với nhau theo kiểu "và" hoặc "hoặc"
            result = params.get(i).isOrPredicate() ? Specification.where(result).or(new EntitySpecification<>(params.get(i))) : Specification.where(result).and(new EntitySpecification<>(params.get(i)));
        }
        return result;
    }

    public EntitySpecificationsBuilder<T> with(EntitySpecification<T> spec) {
        params.add(spec.getCriteria());
        return this;
    }

    public EntitySpecificationsBuilder<T> with(SpecSearchCriteria criteria) {
        params.add(criteria);
        return this;
    }
}

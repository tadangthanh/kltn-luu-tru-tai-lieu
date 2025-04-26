package vn.kltn.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class EntitySpecification <T> implements Specification<T>   {
    private SpecSearchCriteria  criteria;

    // hàm tạo điều kiện tim kiếm
    @Override
    public Predicate toPredicate(@NonNull final Root<T> root, final CriteriaQuery<?> query, @NonNull final CriteriaBuilder builder) {
        if (criteria.getKey().contains(".")) {
            String[] keys = criteria.getKey().split("\\.");
            return builder.equal(root.get(keys[0]).get(keys[1]), criteria.getValue());
        }

        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()),
                    "true".equals(criteria.getValue()) ? true :
                            "false".equals(criteria.getValue()) ? false : criteria.getValue());

            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());

            case BETWEEN -> {
                if (criteria.getValue() instanceof List<?> values && values.size() == 2) {
                    LocalDateTime from = (LocalDateTime) values.get(0);
                    LocalDateTime to = (LocalDateTime) values.get(1);
                    yield builder.between(root.get(criteria.getKey()).as(LocalDateTime.class), from, to);
                }
                yield null;
            }

            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

}

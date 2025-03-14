package vn.kltn.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

@Getter
@AllArgsConstructor
public class EntitySpecification <T> implements Specification<T>   {
    private SpecSearchCriteria  criteria;

    // hàm tạo điều kiện tim kiếm
    @Override
    public Predicate toPredicate(@NonNull final Root<T> root,final CriteriaQuery<?> query,@NonNull final  CriteriaBuilder builder) {
        if(criteria.getKey().split("\\.").length > 1){
            return builder.equal(root.get(criteria.getKey().split("\\.")[0]).get(criteria.getKey().split("\\.")[1]), criteria.getValue());
        }
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue().equals("true")?true:criteria.getValue().equals("false")?false:criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }
}

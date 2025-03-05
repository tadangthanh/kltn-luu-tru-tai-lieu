package vn.kltn.repository.specification;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static vn.kltn.repository.specification.SearchOperation.*;


@Getter
@Setter
@NoArgsConstructor
public class SpecSearchCriteria {
    // field tìm kiếm : ví dụ name, age...
    private String key;
    // toán tử tìm kiếm : equals, lessthan...
    private SearchOperation operation;
    // value tìm kiếm dựa trên field : ví dụ ta dang thanh,  18...
    private Object value;
    // nếu true thì tìm kiếm "hoặc" và false thì tìm kiếm "và"
    private boolean orPredicate;


    public SpecSearchCriteria(final String key, final SearchOperation operation, final Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(final String orPredicate, final String key, final SearchOperation operation, final Object value) {
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(String key, String operation, String value, String prefix, String suffix) {
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOperation != null) {
            if (searchOperation == EQUALITY) { // the operation may be complex operation
                // băt đầu bằng dấu sao
                final boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
                // kết thúc bằng dấu sao
                final boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);
                // thì tìm kiếm kiểu chứa
                if (startWithAsterisk && endWithAsterisk) {
                    searchOperation = CONTAINS;
                } else if (startWithAsterisk) { // nếu chỉ có bắt đầu bằng dấu * thì tìm kiếm theo "kết thúc bằng"
                    searchOperation = ENDS_WITH;
                } else if (endWithAsterisk) {
                    searchOperation = STARTS_WITH;
                }
            }
        }
        this.key = key;
        this.operation = searchOperation;
        this.value = value;
    }
}

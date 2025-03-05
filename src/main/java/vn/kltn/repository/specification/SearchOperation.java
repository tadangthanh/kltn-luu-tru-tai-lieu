package vn.kltn.repository.specification;


public enum SearchOperation {
    EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, LIKE, STARTS_WITH, ENDS_WITH, CONTAINS;

    public static final String[] SIMPLE_OPERATION_SET = {":", "!", ">", "<", "~"};

    public static final String OR_PREDICATE_FLAG = "'";

    public static final String ZERO_OR_MORE_REGEX = "*";

    public static final String OR_OPERATOR = "OR";

    public static final String AND_OPERATOR = "AND";

    public static final String LEFT_PARENTHESIS = "(";

    public static final String RIGHT_PARENTHESIS = ")";

    public static SearchOperation getSimpleOperation(final char input) {
        return switch (input) {
            case ':' -> EQUALITY; //bằng
            case '!' -> NEGATION; // phủ định
            case '>' -> GREATER_THAN; // lớn hơn
            case '<' -> LESS_THAN; // nhỏ hơn
            case '~' -> LIKE; // tồn tại
            default -> null;
        };
    }
}

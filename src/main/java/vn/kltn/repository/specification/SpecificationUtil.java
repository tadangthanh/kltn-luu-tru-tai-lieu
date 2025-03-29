package vn.kltn.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecificationUtil {
    private static final Pattern PATTERN = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");

    public static <T> Specification<T> buildSpecificationFromFilters(String[] filters, EntitySpecificationsBuilder<T> builder) {
        for (String filter : filters) {
            Matcher matcher = PATTERN.matcher(filter);
            if (matcher.find()) {
                builder.with(
                        matcher.group(1), // field
                        matcher.group(2), // operator
                        matcher.group(3), // value
                        matcher.group(4), // optional punctuations
                        matcher.group(5)  // optional punctuations
                );
            }
        }
        return builder.build();
    }
}
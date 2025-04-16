package vn.kltn.validation;

import com.nimbusds.jose.Payload;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipartFileArrayValidator.class)
@Documented
public @interface ValidFiles {
    String message() default "Files không hợp lệ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
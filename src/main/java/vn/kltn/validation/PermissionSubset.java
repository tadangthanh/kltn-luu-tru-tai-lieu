package vn.kltn.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.kltn.common.Permission;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PermissionSubsetValidator.class)
public @interface PermissionSubset {
    Permission[] anyOf();

    String message() default "must be any of {anyOf}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
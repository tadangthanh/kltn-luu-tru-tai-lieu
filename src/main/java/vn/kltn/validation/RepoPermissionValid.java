package vn.kltn.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RepoPermissionValidator.class)
public @interface RepoPermissionValid {

    String message() default "{name} must be any of enum {enumClass}";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    Class<? extends Enum<?>> enumClass();
}

package vn.kltn.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class RepoPermissionValidator implements ConstraintValidator<RepoPermissionValid, List<String>> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(RepoPermissionValid constraintAnnotation) {
        enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        for (String permission : value) {
            boolean isValid = false;
            for (Enum<?> enumValue : enumClass.getEnumConstants()) {
                if (enumValue.name().equals(permission)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                return false;
            }
        }
        return true;
    }
}
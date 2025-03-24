package vn.kltn.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.kltn.common.Permission;

import java.util.Arrays;

public class PermissionSubsetValidator implements ConstraintValidator<PermissionSubset, Permission> {
    private Permission[] permissions;

    @Override
    public void initialize(PermissionSubset constraint) {
        this.permissions = constraint.anyOf();
    }

    @Override
    public boolean isValid(Permission value, ConstraintValidatorContext context) {
        return value == null || Arrays.asList(permissions).contains(value);
    }
}

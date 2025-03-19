package vn.kltn.validation;

import vn.kltn.common.RoleName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MemberHasAnyRole {
    RoleName[] value(); // Truyền vào quyền cần kiểm tra
}

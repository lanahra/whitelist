package com.lanahra.whitelist.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * ValidPattern Annotation
 * Use the ValidPattern annotation to validate regular expression String
 *
 * @see ValidPatternValidation
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=ValidPatternValidator.class)
public @interface ValidPattern {

    String message() default "not a valid regex";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

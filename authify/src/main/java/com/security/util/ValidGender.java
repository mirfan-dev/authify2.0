package com.security.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD}) // Specifies that the annotation can be applied to fields.
@Retention(RetentionPolicy.RUNTIME) // The annotation is available at runtime.
@Constraint(validatedBy = GenderValidator.class) // Links the annotation to its validator.
public @interface ValidGender {

    /**
     * Error message to be returned when validation fails.
     */
    String message() default "Invalid Gender";

    /**
     * Groups for categorizing validations.
     */
    Class<?>[] groups() default {};

    /**
     * Payload for additional metadata about the validation.
     */
    Class<? extends Payload>[] payload() default {};

}

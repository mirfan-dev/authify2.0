package com.security.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class GenderValidator implements ConstraintValidator<ValidGender,String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if(value==null || value.isEmpty()){
            return false;
        }
        if (value.equalsIgnoreCase("male") ||
                value.equalsIgnoreCase("female") ||
                value.equalsIgnoreCase("other")) {
            return true;
        }

        return false;
    }
}

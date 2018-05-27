package com.lanahra.whitelist.validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPatternValidator implements ConstraintValidator<ValidPattern, String> {

    @Override
    public void initialize(ValidPattern validPattern) {
    }

    @Override
    public boolean isValid(String regex, ConstraintValidatorContext context) {

        if (regex == null) {
            return false;
        }

        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return false;
        }

        return true;
    }
}

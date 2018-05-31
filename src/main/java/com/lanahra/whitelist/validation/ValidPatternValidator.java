package com.lanahra.whitelist.validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Implementation of ValidPattern Annotation
 */
public class ValidPatternValidator implements ConstraintValidator<ValidPattern, String> {

    @Override
    public void initialize(ValidPattern validPattern) {
    }

    /**
     * @return true if regular expression is null or valid
     * @return false if regular expression is not valid
     */
    @Override
    public boolean isValid(String regex, ConstraintValidatorContext context) {

        if (regex == null) {
            return true;
        }

        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return false;
        }

        return true;
    }
}

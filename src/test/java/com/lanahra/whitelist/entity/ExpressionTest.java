package com.lanahra.whitelist.entity;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExpressionTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testRegexNull() {
        Expression expression = new Expression();
        expression.setClient("client");

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("must not be null");
    }

    @Test
    public void testRegexBlank() {
        Expression expression = new Expression();
        expression.setClient("client");
        expression.setRegex("");

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testRegexTooLong() {
        Expression expression = new Expression();
        expression.setClient("client");

        // 129 characters String
        expression.setRegex(
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnopq"
        );

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testRegexInvalid() {
        Expression expression = new Expression();
        expression.setClient("client");
        expression.setRegex("(abc))");

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("not a valid regex");
    }

    @Test
    public void testClientBlank() {
        Expression expression = new Expression();
        expression.setClient("");
        expression.setRegex("abc");

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testClientTooLong() {
        Expression expression = new Expression();

        // 129 characters String
        expression.setClient(
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnopq"
        );

        expression.setRegex("abc");

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testValid() {
        Expression expression = new Expression();
        expression.setClient("client");
        expression.setRegex("ab.*ba");

        Set<ConstraintViolation<Expression>> violations = validator.validate(expression);

        assertThat(violations.size()).isEqualTo(0);
    }
}

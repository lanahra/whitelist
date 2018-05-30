package com.lanahra.whitelist.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class ValidationRequestTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testClientNull() {
        ValidationRequest request = new ValidationRequest();
        request.setUrl("url");
        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("must not be null");
    }

    @Test
    public void testUrlNull() {
        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("must not be null");
    }

    @Test
    public void testCorrelationIdNull() {
        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("url");

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("must not be null");
    }

    @Test
    public void testClientBlank() {
        ValidationRequest request = new ValidationRequest();
        request.setClient("");
        request.setUrl("url");
        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testUrlBlank() {
        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("");
        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testClientTooLong() {
        ValidationRequest request = new ValidationRequest();

        // 129 characters String
        request.setClient(
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnopq"
        );

        request.setUrl("url");
        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testUrlTooLong() {
        ValidationRequest request = new ValidationRequest();

        request.setClient("client");

        // 129 characters String
        request.setUrl(
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnop" +
            "abcdefghijklmnopq"
        );

        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(1);

        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("size must be between 1 and 128");
    }

    @Test
    public void testValid() {
        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("url");
        request.setCorrelationId(0);

        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isEqualTo(0);
    }
}

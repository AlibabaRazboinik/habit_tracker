package com.example.habitstracker.validation.annotations;

import com.example.habitstracker.constants.ValidationConstants;
import com.example.habitstracker.validation.validators.AtLeastOneDigitValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Аннотация, которая позволяет проверить, что в поле содержится как минимум одна цифра
 */
@Documented
@Constraint(validatedBy = AtLeastOneDigitValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneDigit {
    String message() default ValidationConstants.AT_LEAST_ONE_DIGIT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

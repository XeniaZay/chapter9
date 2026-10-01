package com.example.validation.period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PeriodValidator implements ConstraintValidator<ValidPeriod, ReportPeriodRequest> {
    @Override
    public boolean isValid(ReportPeriodRequest value, ConstraintValidatorContext context) {

        if (value == null) return true;
        if (value.from() == null || value.to() == null) return true;

        boolean valid = value.from().isBefore(value.to());

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("to")
                    .addConstraintViolation();
        }

        return valid;
    }
}

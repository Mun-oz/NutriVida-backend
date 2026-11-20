package cl.nutrivida.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunConstraintValidator
        implements ConstraintValidator<RunValido, String> {

    private boolean required;

    @Override
    public void initialize(RunValido annotation) {
        this.required = annotation.required();
    }

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return !required;
        }

        return RunUtils.esValido(value);
    }
}

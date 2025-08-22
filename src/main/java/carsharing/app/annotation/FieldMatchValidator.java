package carsharing.app.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstPasswordName;
    private String secondPasswordName;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        firstPasswordName = constraintAnnotation.firstPasswordName();
        secondPasswordName = constraintAnnotation.secondPasswordName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object firstValue = new BeanWrapperImpl(value).getPropertyValue(firstPasswordName);
        Object secondValue = new BeanWrapperImpl(value).getPropertyValue(secondPasswordName);
        return Objects.equals(firstValue, secondValue);
    }
}

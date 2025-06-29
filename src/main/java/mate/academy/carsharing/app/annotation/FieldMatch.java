package mate.academy.carsharing.app.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldMatchValidator.class)
public @interface FieldMatch {
    String message() default "Password and repeat password must be the same";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String firstPasswordName();

    String secondPasswordName();
}

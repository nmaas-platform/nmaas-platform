package net.geant.nmaas.portal.persistence.entity.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNullIfAnotherFieldIsNullValidator.class)
public @interface ValidUser {

    String message() default "User entity needs email or external identifier";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

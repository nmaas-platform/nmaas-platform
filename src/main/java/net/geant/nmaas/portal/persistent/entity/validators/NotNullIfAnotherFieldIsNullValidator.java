package net.geant.nmaas.portal.persistent.entity.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import net.geant.nmaas.portal.persistent.entity.User;

public class NotNullIfAnotherFieldIsNullValidator implements ConstraintValidator<ValidUser, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if(!(value instanceof User)){
            throw new IllegalArgumentException("@ValidUser can be applied only for User entity");
        }
        User user = (User) value;
        if(checkParam(user.getSamlToken()) && checkParam(user.getEmail())){
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(user.getEmail())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean checkParam(String value){
        return value == null || value.isEmpty();
    }
}

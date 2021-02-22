package net.geant.nmaas.utils.captcha;

import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CaptchaValidatorAspect {
    private CaptchaValidator captchaValidator;

    @Autowired
    public CaptchaValidatorAspect(CaptchaValidator captchaValidator){
        this.captchaValidator = captchaValidator;
    }

    @Before(value="@annotation(validateCaptcha)", argNames = "joinPoint, validateCaptcha")
    public void validateCaptcha(JoinPoint joinPoint, ValidateCaptcha validateCaptcha){
        if(!this.captchaValidator.verifyToken(joinPoint.getArgs()[1].toString())){
            throw new TokenAuthenticationException("Captcha validation has failed");
        }
    }
}

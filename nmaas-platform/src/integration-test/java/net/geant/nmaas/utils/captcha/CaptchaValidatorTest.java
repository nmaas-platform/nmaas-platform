package net.geant.nmaas.utils.captcha;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CaptchaValidatorTest {

    @Autowired
    private CaptchaValidator captchaValidator;

    /**
     * Disabled because of using captcha key that validates every token, to be able to test captcha-protected endpoints
     * This is no longer required
     */
    @Disabled
    @Test
    void shouldNotValidateInvalidToken(){
        assertFalse(captchaValidator.verifyToken("invalidToken"));
    }
}

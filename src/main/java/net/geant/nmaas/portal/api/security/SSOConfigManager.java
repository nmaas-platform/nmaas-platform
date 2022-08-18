package net.geant.nmaas.portal.api.security;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.auth.SSOView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@Getter
@Component
@ApplicationScope
@Log4j2
public class SSOConfigManager {

    @Value("${sso.loginUrl}")
    private String loginUrl;

    @Value("${sso.logoutUrl}")
    private String logoutUrl;

    @Value("${sso.timeout}")
    private Integer timeout;

    @Value("${sso.key}")
    private String key;

    public void validateConfig(boolean ssoLoginAllowed) {
        if (ssoLoginAllowed) {
            if (this.loginUrl == null || this.loginUrl.isEmpty()) {
                throw new IllegalStateException("Login url cannot be null or empty");
            }
            if (this.logoutUrl == null || this.logoutUrl.isEmpty()) {
                throw new IllegalStateException("Logout url cannot be null or empty");
            }
            if (this.key == null || this.key.isEmpty()) {
                throw new IllegalStateException("Key cannot be null or empty");
            }
            if (this.timeout < 0) {
                throw new IllegalStateException("Timeout cannot be less than 0");
            }
        } else {
            log.debug("SSO login option is disabled. Skipping SSO configuration validation");
        }
    }

    public boolean isConfigValid(boolean ssoLoginAllowed) {
        try {
            validateConfig(ssoLoginAllowed);
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            return false;
        }
        return true;
    }

    public SSOView getSSOView(){
        return new SSOView(loginUrl, logoutUrl);
    }

}

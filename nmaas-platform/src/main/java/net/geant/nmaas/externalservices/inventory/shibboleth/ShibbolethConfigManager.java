package net.geant.nmaas.externalservices.inventory.shibboleth;

import lombok.Getter;
import net.geant.nmaas.externalservices.inventory.shibboleth.model.ShibbolethView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@Getter
@Component
@ApplicationScope
public class ShibbolethConfigManager {

    @Value("${sso.loginUrl}")
    private String loginUrl;

    @Value("${sso.logoutUrl}")
    private String logoutUrl;

    @Value("${sso.timeout}")
    private int timeout;

    @Value("${sso.key}")
    private String key;

    public void checkParam(){
        if(this.loginUrl == null || this.loginUrl.isEmpty())
            throw new IllegalStateException("Login url cannot be null or empty");
        if(this.logoutUrl == null || this.logoutUrl.isEmpty())
            throw new IllegalStateException("Logout url cannot be null or empty");
        if(this.key == null || this.key.isEmpty())
            throw new IllegalStateException("Key cannot be null or empty");
        if(this.timeout < 0)
            throw new IllegalStateException("Timeout cannot be less than 0");
    }

    ShibbolethView getShibbolethView(){
        return new ShibbolethView(loginUrl, logoutUrl);
    }

}

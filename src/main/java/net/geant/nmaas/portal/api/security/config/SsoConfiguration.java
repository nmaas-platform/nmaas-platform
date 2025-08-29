package net.geant.nmaas.portal.api.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConditionalOnProperty(
        value = "portal.config.ssoLoginAllowed",
        havingValue = "true"
)
@PropertySource("classpath:application-oidc.properties")
public class SsoConfiguration {
}

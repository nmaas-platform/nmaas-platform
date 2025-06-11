package net.geant.nmaas.security;

import net.geant.nmaas.portal.api.security.SkipPathRequestMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SecurityConstants {

    private SecurityConstants() {
    }

    static final String SSL_ENABLED = "server.ssl.enabled";

    static final String AUTH_BASIC_LOGIN = "/api/auth/basic/login";
    static final String AUTH_BASIC_SIGNUP = "/api/auth/basic/registration/**";
    static final String AUTH_BASIC_TOKEN = "/api/auth/basic/token";

    static final String AUTH_SSO_LOGIN = "/api/auth/sso/login";
    static final String AUTH_OIDC_LOGIN_PAGE = "/api/oauth2/authorization/my-oidc";
    static final String AUTH_OIDC_LOGIN = "/api/auth/oidc/login";
    static final String AUTH_OIDC_SUCCESS = "/api/oidc/success";
    static final String AUTH_OIDC_LINK = "/api/oidc/link";
    static final String AUTH_LOGOUT = "/api/oidc/logout/*";
    static final String AUTH_OIDC = "/api/oidc/**";
    static final String AUTH_CODE = "/api/login/oauth2/code";

    static final RequestMatcher[] AUTH_WHITELIST = {
            new AntPathRequestMatcher(AUTH_BASIC_LOGIN),
            new AntPathRequestMatcher(AUTH_BASIC_SIGNUP),
            new AntPathRequestMatcher(AUTH_BASIC_TOKEN),
            new AntPathRequestMatcher(AUTH_SSO_LOGIN),
            new AntPathRequestMatcher(AUTH_OIDC_LOGIN),
            new AntPathRequestMatcher(AUTH_OIDC_LOGIN_PAGE),
            new AntPathRequestMatcher(AUTH_OIDC),
            new AntPathRequestMatcher(AUTH_LOGOUT),
            new AntPathRequestMatcher(AUTH_CODE),
            new AntPathRequestMatcher("/favicon.ico"),
            new AntPathRequestMatcher("/api/info/**"),
            new AntPathRequestMatcher("/actuator/**"),
            new AntPathRequestMatcher("/api/content/**"),
            new AntPathRequestMatcher("/api/users/reset/**"),
            new AntPathRequestMatcher("/api/mail"),
            new AntPathRequestMatcher("/api-docs/**"),
            new AntPathRequestMatcher("/api/**", HttpMethod.OPTIONS.name()),
            new AntPathRequestMatcher("/api/orchestration/deployments/**", HttpMethod.OPTIONS.name()),
            new AntPathRequestMatcher("/api/orchestration/deployments/**/state", HttpMethod.OPTIONS.name()),
            new AntPathRequestMatcher("/api/orchestration/deployments/**/access", HttpMethod.OPTIONS.name()),
            new AntPathRequestMatcher("/api/management", HttpMethod.OPTIONS.name()),
            new AntPathRequestMatcher("/api/management", HttpMethod.OPTIONS.name()),
            new AntPathRequestMatcher("/api/i18n/content/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/i18n/all/enabled", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/configuration/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/auth/sso", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/mail/type", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/monitor/all", HttpMethod.GET.name())
    };

    static final RequestMatcher[] AUTH_AUTHENTICATED_LIST = {
            new AntPathRequestMatcher("/api/orchestration/deployments/**/state"),
            new AntPathRequestMatcher("/api/orchestration/deployments/**/access"),
            new AntPathRequestMatcher("/api/orchestration/deployments/**"),
            new AntPathRequestMatcher("/api/management/**"),
            new AntPathRequestMatcher("/api/**")
    };

    static final SkipPathRequestMatcher skipPathRequestMatcher =
            new SkipPathRequestMatcher(
                    new AntPathRequestMatcher[]{
                            new AntPathRequestMatcher(AUTH_BASIC_LOGIN),
                            new AntPathRequestMatcher(AUTH_BASIC_SIGNUP),
                            new AntPathRequestMatcher(AUTH_BASIC_TOKEN),
                            new AntPathRequestMatcher(AUTH_SSO_LOGIN),
                            new AntPathRequestMatcher(AUTH_OIDC_LOGIN),
                            new AntPathRequestMatcher(AUTH_OIDC_LOGIN_PAGE),
                            new AntPathRequestMatcher(AUTH_OIDC),
                            new AntPathRequestMatcher(AUTH_CODE),
                            new AntPathRequestMatcher("/api-docs/**"),
                            new AntPathRequestMatcher("/actuator/**"),
                            new AntPathRequestMatcher("/favicon.ico"),
                            new AntPathRequestMatcher("/api/configuration/**", "GET"),
                            new AntPathRequestMatcher("/api/auth/sso", "GET"),
                            new AntPathRequestMatcher("/api/info/**"),
                            new AntPathRequestMatcher("/api/dcns/notifications/**/status"),
                            new AntPathRequestMatcher("/api/content/**"),
                            new AntPathRequestMatcher("/api/users/reset/**"),
                            new AntPathRequestMatcher("/api/mail"),
                            new AntPathRequestMatcher("/api/monitor/all", "GET"),
                            new AntPathRequestMatcher("/api/mail/type", "GET"),
                            new AntPathRequestMatcher("/api/i18n/content/**", "GET"),
                            new AntPathRequestMatcher("/api/i18n/all/enabled", "GET"),
                            new AntPathRequestMatcher("/api/gitlab/webhooks/**")
                    }
            );
}

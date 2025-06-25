package net.geant.nmaas.security;

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


    protected static final String[] AUTH_WHITELIST = {
            AUTH_BASIC_LOGIN,
            AUTH_BASIC_SIGNUP,
            AUTH_BASIC_TOKEN,
            AUTH_SSO_LOGIN,
            AUTH_OIDC_LOGIN,
            AUTH_OIDC_LOGIN_PAGE,
            AUTH_OIDC,
            AUTH_LOGOUT,
            AUTH_CODE,
            AUTH_OIDC_LINK,
            "/favicon.ico",
            "/api/info/**",
            "/actuator/**",
            "/api/content/**",
            "/api/users/reset/**",
            "/api/mail",
            "/api-docs/**",
            "/api/**",
            "/api/orchestration/deployments/**",
            "/api/orchestration/deployments/**/state",
            "/api/orchestration/deployments/**/access",
            "/api/management",
            "/api/i18n/content/**",
            "/api/i18n/all/enabled",
            "/api/configuration/**",
            "/api/auth/sso",
            "/api/mail/type",
            "/api/monitor/all"
    };

    protected static final String[] AUTH_AUTHENTICATED_LIST = {
            "/api/orchestration/deployments/**/state",
            "/api/orchestration/deployments/**/access",
            "/api/orchestration/deployments/**",
            "/api/management/**",
            "/api/**"
    };
    protected static final String[] SKIPPED_PATHS = {
            AUTH_BASIC_LOGIN,
            AUTH_BASIC_SIGNUP,
            AUTH_BASIC_TOKEN,
            AUTH_SSO_LOGIN,
            AUTH_OIDC_LOGIN,
            AUTH_OIDC_LOGIN_PAGE,
            AUTH_OIDC,
            AUTH_CODE,
            "/api-docs/**",
            "/actuator/**",
            "/favicon.ico",
            "/api/configuration/**",
            "/api/auth/sso",
            "/api/info/**",
            "/api/dcns/notifications/**/status",
            "/api/content/**",
            "/api/users/reset/**",
            "/api/mail",
            "/api/monitor/all",
            "/api/mail/type",
            "/api/i18n/content/**",
            "/api/i18n/all/enabled",
            "/api/gitlab/webhooks/**"
    };
}

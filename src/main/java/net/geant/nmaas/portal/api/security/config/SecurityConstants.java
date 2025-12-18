package net.geant.nmaas.portal.api.security.config;

import java.util.List;
import java.util.Map;

/**
 * Contains constant values related to security configuration, such as
 * authentication endpoints, CORS-exempted paths, and whitelist patterns.
 * <p>
 * This class is not meant to be instantiated.
 */
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
    static final String AUTH_OIDC_APPROVALS = "/api/oidc/approvals";
    static final String AUTH_LOGOUT = "/api/oidc/logout/*";
    static final String AUTH_OIDC = "/api/oidc/**";
    static final String AUTH_CODE = "/api/login/oauth2/code";

    /**
     * Paths allowed for unauthenticated GET requests (e.g., for public config or localization).
     */
    static final String[] AUTH_WHITELIST_GET_METHOD = {
            "/api/i18n/content/**",
            "/api/i18n/all/enabled",
            "/api/configuration/**",
            "/api/auth/sso",
            "/api/mail/type",
            "/api/monitor/all"
    };

    /**
     * Paths whitelisted for unauthenticated OPTIONS requests (CORS preflight, etc.).
     */
    static final String[] AUTH_WHITELIST_OPTIONS_METHOD = {
            "/api/**"
    };

    /**
     * Paths whitelisted for any HTTP method without authentication.
     */
    protected static final String[] AUTH_WHITELIST_ANY_METHOD = {
            AUTH_BASIC_LOGIN,
            AUTH_BASIC_SIGNUP,
            AUTH_BASIC_TOKEN,
            AUTH_SSO_LOGIN,
            AUTH_OIDC_LOGIN,
            AUTH_OIDC_LOGIN_PAGE,
            AUTH_OIDC,
            AUTH_OIDC_APPROVALS,
            AUTH_LOGOUT,
            AUTH_CODE,
            AUTH_OIDC_LINK,
            "/favicon.ico",
            "/api/info/**",
            "/actuator/**",
            "/api/content/**",
            "/api/users/reset/**",
            "/api/mail",
            "/api-docs/**"
    };
    /**
     * Paths whitelisted for authentication for specific protected resources.
     */
    //TODO check that this function will work without commented strings
    protected static final String[] AUTH_AUTHENTICATED_LIST = {
            "/api/**"
    };
    /**
     * Map of HTTP methods to paths that should skip JWT authentication filters.
     */
    static final Map<String, List<String>> SKIPPED_PATHS = Map.of(
            "GET", List.of(
                    "/api/configuration/**",
                    "/api/auth/sso",
                    "/api/monitor/all",
                    "/api/mail/type",
                    "/api/i18n/content/**",
                    "/api/i18n/all/enabled"
            ),
            "ANY", List.of(
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
                    "/api/info/**",
                    "/api/dcns/notifications/**/status",
                    "/api/content/**",
                    "/api/users/reset/**",
                    "/api/mail",
                    "/api/gitlab/webhooks/**"
            )
    );

    /**
     * Paths that are protected and require UUID-based token authentication.
     */
    static final String[] AUTH_UUID_AUTHENTICATED_LIST = {
            "/api/**"
    };
}

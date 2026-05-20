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

    static final String API_VERSIONED_PATTERN = "/api/*";
    static final String API_ANY = API_VERSIONED_PATTERN + "/**";

    static final String AUTH_BASIC_LOGIN = API_VERSIONED_PATTERN + "/auth/basic/login";
    static final String AUTH_BASIC_SIGNUP = API_VERSIONED_PATTERN + "/auth/basic/registration/**";
    static final String AUTH_BASIC_TOKEN = API_VERSIONED_PATTERN + "/auth/basic/token";

    static final String AUTH_SSO_LOGIN = API_VERSIONED_PATTERN + "/auth/sso/login";
    static final String AUTH_OIDC_LOGIN_PAGE = API_VERSIONED_PATTERN + "/oauth2/authorization/my-oidc";
    static final String AUTH_OIDC_LOGIN = API_VERSIONED_PATTERN + "/auth/oidc/login";
    static final String AUTH_OIDC_SUCCESS = API_VERSIONED_PATTERN + "/oidc/success";
    static final String AUTH_OIDC_LINK = API_VERSIONED_PATTERN + "/oidc/link";
    static final String AUTH_OIDC_APPROVALS = API_VERSIONED_PATTERN + "/oidc/approvals";
    static final String AUTH_LOGOUT = API_VERSIONED_PATTERN + "/oidc/logout/*";
    static final String AUTH_OIDC = API_VERSIONED_PATTERN + "/oidc/**";
    static final String AUTH_CODE = API_VERSIONED_PATTERN + "/login/oauth2/code";

    /**
     * Paths allowed for unauthenticated GET requests (e.g., for public config or localization).
     */
    static final String[] AUTH_WHITELIST_GET_METHOD = {
            API_VERSIONED_PATTERN + "/i18n/content/**",
            API_VERSIONED_PATTERN + "/i18n/all/enabled",
            API_VERSIONED_PATTERN + "/configuration/**",
            API_VERSIONED_PATTERN + "/auth/sso",
            API_VERSIONED_PATTERN + "/mail/type",
            API_VERSIONED_PATTERN + "/monitor/all"
    };

    /**
     * Paths whitelisted for unauthenticated OPTIONS requests (CORS preflight, etc.).
     */
    static final String[] AUTH_WHITELIST_OPTIONS_METHOD = {
            API_ANY
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
            API_VERSIONED_PATTERN + "/info/**",
            "/actuator/**",
            API_VERSIONED_PATTERN + "/content/**",
            API_VERSIONED_PATTERN + "/users/reset/**",
            API_VERSIONED_PATTERN + "/mail",
            "/api-docs/**"
    };
    /**
     * Paths whitelisted for authentication for specific protected resources.
     */
    //TODO check that this function will work without commented strings
    protected static final String[] AUTH_AUTHENTICATED_LIST = {
            API_ANY
    };
    /**
     * Map of HTTP methods to paths that should skip JWT authentication filters.
     */
    static final Map<String, List<String>> SKIPPED_PATHS = Map.of(
            "GET", List.of(
                    API_VERSIONED_PATTERN + "/configuration/**",
                    API_VERSIONED_PATTERN + "/auth/sso",
                    API_VERSIONED_PATTERN + "/monitor/all",
                    API_VERSIONED_PATTERN + "/mail/type",
                    API_VERSIONED_PATTERN + "/i18n/content/**",
                    API_VERSIONED_PATTERN + "/i18n/all/enabled"
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
                    API_VERSIONED_PATTERN + "/info/**",
                    API_VERSIONED_PATTERN + "/dcns/notifications/**/status",
                    API_VERSIONED_PATTERN + "/content/**",
                    API_VERSIONED_PATTERN + "/users/reset/**",
                    API_VERSIONED_PATTERN + "/mail",
                    API_VERSIONED_PATTERN + "/gitlab/webhooks/**"
            )
    );

    /**
     * Paths that are protected and require UUID-based token authentication.
     */
    static final String[] AUTH_UUID_AUTHENTICATED_LIST = {
            API_ANY
    };
}

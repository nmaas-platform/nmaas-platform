package net.geant.nmaas;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.configuration.api.security.StatelessGitlabAuthenticationFilter;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import net.geant.nmaas.portal.api.security.SkipPathRequestMatcher;
import net.geant.nmaas.portal.api.security.StatelessAuthenticationFilter;
import net.geant.nmaas.portal.service.TokenAuthenticationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@PropertySource("classpath:application.properties")
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@ComponentScan(basePackages = {"net.geant.nmaas.portal.api.security"})
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String SSL_ENABLED = "server.ssl.enabled";

    private static final String AUTH_BASIC_LOGIN = "/api/auth/basic/login";
    private static final String AUTH_BASIC_SIGNUP = "/api/auth/basic/registration/**";
    private static final String AUTH_BASIC_TOKEN = "/api/auth/basic/token";

    private static final String AUTH_SSO_LOGIN = "/api/auth/sso/login";
    private static final String AUTH_OIDC_LOGIN_PAGE = "/api/oauth2/authorization/my-oidc";
    private static final String AUTH_OIDC_LOGIN = "/api/auth/oidc/login";
    private static final String AUTH_OIDC_SUCCESS = "/api/oidc/success";
    private static final String AUTH_OIDC_LINK = "/api/oidc/link";
    private static final String AUTH_LOGOUT = "/api/oidc/logout/*";
    private static final String AUTH_OIDC = "/api/oidc/**";
    private static final String AUTH_CODE = "/api/login/oauth2/code";

    private final TokenAuthenticationService tokenAuthenticationService;

    private final Environment env;

    private final PasswordEncoder passwordEncoder;

    private final GitLabProjectRepository gitLabProjectRepository;

    private static final RequestMatcher[] AUTH_WHITELIST = {
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

    private static final RequestMatcher[] AUTH_AUTHENTICATED_LIST = {
            new AntPathRequestMatcher("/api/orchestration/deployments/**/state"),
            new AntPathRequestMatcher("/api/orchestration/deployments/**/access"),
            new AntPathRequestMatcher("/api/orchestration/deployments/**"),
            new AntPathRequestMatcher("/api/management/**"),
            new AntPathRequestMatcher("/api/**")
    };

    private static final SkipPathRequestMatcher skipPathRequestMatcher =
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

    @Bean
    @ConditionalOnProperty(name = "portal.config.ssoLoginAllowed", havingValue = "true")
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/api/oauth2/authorization");

        return httpSecurity
//                .exceptionHandling(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(httpRequest -> httpRequest
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(AUTH_OIDC_LINK).permitAll()
                        .requestMatchers(AUTH_AUTHENTICATED_LIST).authenticated()
                )
                .oauth2Login(oAuth2 -> oAuth2
                        .userInfoEndpoint(Customizer.withDefaults())
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(resolver)
                        )
                        .defaultSuccessUrl(AUTH_OIDC_SUCCESS, true)
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/login/oauth2/code/*")
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(statelessAuthFilter(skipPathRequestMatcher,
                                null,
                                tokenAuthenticationService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        gitlabTokenFilter("/api/gitlab/webhooks/**",
                                null,
                                gitLabProjectRepository),
                        StatelessAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "portal.config.ssoLoginAllowed", havingValue = "false")
    public SecurityFilterChain securityFilterChainBasic(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(httpRequest -> httpRequest
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(AUTH_AUTHENTICATED_LIST).authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(statelessAuthFilter(skipPathRequestMatcher,
                                null,
                                tokenAuthenticationService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        gitlabTokenFilter("/api/gitlab/webhooks/**",
                                null,
                                gitLabProjectRepository),
                        StatelessAuthenticationFilter.class
                )
                .build();
    }

    private Filter statelessAuthFilter(RequestMatcher skipPaths, AuthenticationFailureHandler failureHandler, TokenAuthenticationService tokenService) {
        StatelessAuthenticationFilter filter = new StatelessAuthenticationFilter(skipPaths, tokenService);
        if (failureHandler != null) {
            filter.setAuthenticationFailureHandler(failureHandler);
        }
        return filter;
    }

    private Filter gitlabTokenFilter(String url, AuthenticationFailureHandler failureHandler, GitLabProjectRepository gitLabProjectRepository) {
        StatelessGitlabAuthenticationFilter filter = new StatelessGitlabAuthenticationFilter(url, gitLabProjectRepository);
        if (failureHandler != null) {
            filter.setAuthenticationFailureHandler(failureHandler);
        }
        return filter;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();

        //TODO: customize CORS through properties. Currently CORS is enabled for /api
        corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L); // Set max age for preflight requests

        source.registerCorsConfiguration("/api/**", corsConfig);

        return new CorsFilter(source);
    }

}

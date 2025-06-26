package net.geant.nmaas.security;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.configuration.api.security.StatelessGitlabAuthenticationFilter;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
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

    private final TokenAuthenticationService tokenAuthenticationService;

    private final Environment env;

    private final PasswordEncoder passwordEncoder;

    private final GitLabProjectRepository gitLabProjectRepository;

    public static final SkipPathRequestMatcher skipPathRequestMatcher =
            new SkipPathRequestMatcher(SecurityConstants.SKIPPED_PATHS);

    @Bean
    @ConditionalOnProperty(name = "portal.config.ssoLoginAllowed", havingValue = "true")
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/api/oauth2/authorization");

        return httpSecurity
//                .exceptionHandling(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(SecurityConstants.AUTH_WHITELIST_ANY_METHOD).permitAll()
                        .requestMatchers(HttpMethod.GET ,SecurityConstants.AUTH_WHITELIST_GET_METHOD).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS ,SecurityConstants.AUTH_WHITELIST_OPTIONS_METHOD).permitAll()
                        .requestMatchers(SecurityConstants.AUTH_AUTHENTICATED_LIST).authenticated()
                )
                .oauth2Login(oAuth2 -> oAuth2
                        .userInfoEndpoint(Customizer.withDefaults())
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(resolver)
                        )
                        .defaultSuccessUrl(SecurityConstants.AUTH_OIDC_SUCCESS, true)
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/login/oauth2/code/*")
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
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
                        .requestMatchers(SecurityConstants.AUTH_WHITELIST_ANY_METHOD).permitAll()
                        .requestMatchers(HttpMethod.GET ,SecurityConstants.AUTH_WHITELIST_GET_METHOD).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS ,SecurityConstants.AUTH_WHITELIST_OPTIONS_METHOD).permitAll()
                        .requestMatchers(SecurityConstants.AUTH_AUTHENTICATED_LIST).authenticated()
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

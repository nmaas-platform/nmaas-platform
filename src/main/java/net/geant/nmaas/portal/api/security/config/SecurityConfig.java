package net.geant.nmaas.portal.api.security.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.nmservice.configuration.api.security.StatelessGitlabAuthenticationFilter;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import net.geant.nmaas.portal.api.security.StatelessAuthenticationFilter;
import net.geant.nmaas.portal.api.security.StatelessUUIDAuthenticationFilter;
import net.geant.nmaas.portal.service.TokenAuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
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
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@PropertySource("classpath:application.properties")
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@ComponentScan(basePackages = {"net.geant.nmaas.portal.api.security"})
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final TokenAuthenticationService tokenAuthenticationService;

    private final Environment env;

    private final PasswordEncoder passwordEncoder;

    private final GitLabProjectRepository gitLabProjectRepository;

    public static final SkipPathRequestMatcher skipPathRequestMatcher =
            new SkipPathRequestMatcher(SecurityConstants.SKIPPED_PATHS);


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        boolean ssoEnabled = Boolean.parseBoolean(env.getProperty("portal.config.ssoLoginAllowed", "false"));

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(SecurityConstants.AUTH_WHITELIST_ANY_METHOD).permitAll();
                    auth.requestMatchers(HttpMethod.GET, SecurityConstants.AUTH_WHITELIST_GET_METHOD).permitAll();
                    auth.requestMatchers(HttpMethod.OPTIONS, SecurityConstants.AUTH_WHITELIST_OPTIONS_METHOD).permitAll();
                    auth.requestMatchers(SecurityConstants.AUTH_AUTHENTICATED_LIST).authenticated();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(gitlabTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter(), StatelessGitlabAuthenticationFilter.class)
                .addFilterBefore(uuidAuthFilter(), StatelessAuthenticationFilter.class)
        ;

        if (ssoEnabled) {
            DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository, "/api/oauth2/authorization");

            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(Customizer.withDefaults())
                    .authorizationEndpoint(authorization -> authorization
                            .authorizationRequestResolver(resolver))
                    .defaultSuccessUrl(SecurityConstants.AUTH_OIDC_SUCCESS, true)
                    .redirectionEndpoint(redirection ->
                            redirection.baseUri("/api/login/oauth2/code/*"))
            );
        }

        return http.build();
    }

    private Filter gitlabTokenFilter() {
        var filter = new StatelessGitlabAuthenticationFilter("/api/gitlab/webhooks/**", gitLabProjectRepository);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }

    private Filter uuidAuthFilter() {
        var parser = new PathPatternParser();
        List<Predicate<HttpServletRequest>> matchers = Arrays.stream(SecurityConstants.AUTH_UUID_AUTHENTICATED_LIST)
                .map(parser::parse)
                .map(pattern -> (Predicate<HttpServletRequest>) request ->
                        pattern.matches(PathContainer.parsePath(request.getRequestURI())))
                .toList();

        var filter = new StatelessUUIDAuthenticationFilter(matchers, tokenAuthenticationService);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }


    private Filter jwtAuthFilter() {
        var filter = new StatelessAuthenticationFilter(skipPathRequestMatcher, tokenAuthenticationService);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
            response.flushBuffer();

        };
    }

    @Bean
    public HandlerMappingIntrospector handlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
}

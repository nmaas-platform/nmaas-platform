package net.geant.nmaas.security;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.configuration.api.security.StatelessGitlabAuthenticationFilter;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import net.geant.nmaas.portal.api.security.StatelessAuthenticationFilter;
import net.geant.nmaas.portal.service.TokenAuthenticationService;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        boolean ssoEnabled = Boolean.parseBoolean(System.getProperty("portal.config.ssoLoginAllowed", "false"));

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers(SecurityConstants.AUTH_WHITELIST_ANY_METHOD).permitAll();
                    authz.requestMatchers(HttpMethod.GET, SecurityConstants.AUTH_WHITELIST_GET_METHOD).permitAll();
                    authz.requestMatchers(HttpMethod.OPTIONS, SecurityConstants.AUTH_WHITELIST_OPTIONS_METHOD).permitAll();
                    authz.requestMatchers(SecurityConstants.AUTH_AUTHENTICATED_LIST).authenticated();
                    authz.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(statelessAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gitlabTokenFilter(), StatelessAuthenticationFilter.class);

        if (ssoEnabled) {
            var resolver = new DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository, "/api/oauth2/authorization");

            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(Customizer.withDefaults())
                    .authorizationEndpoint(authorization -> authorization
                            .authorizationRequestResolver(resolver))
                    .defaultSuccessUrl(SecurityConstants.AUTH_OIDC_SUCCESS, true)
                    .redirectionEndpoint(redir -> redir.baseUri("/api/login/oauth2/code/*"))
            );
        }

        return http.build();
    }

    private Filter statelessAuthFilter() {
        var filter = new StatelessAuthenticationFilter(skipPathRequestMatcher, tokenAuthenticationService);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }

    private Filter gitlabTokenFilter() {
        var filter = new StatelessGitlabAuthenticationFilter("/api/gitlab/webhooks/**", gitLabProjectRepository);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
        };
    }
}

package net.geant.nmaas;

import net.geant.nmaas.nmservice.configuration.api.security.StatelessGitlabAuthenticationFilter;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import net.geant.nmaas.portal.api.security.JWTSettings;
import net.geant.nmaas.portal.api.security.RestAuthenticationEntryPoint;
import net.geant.nmaas.portal.api.security.SkipPathRequestMatcher;
import net.geant.nmaas.portal.api.security.StatelessAuthenticationFilter;
import net.geant.nmaas.portal.service.TokenAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySource("classpath:application.properties")
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@ComponentScan(basePackages = {"net.geant.nmaas.portal.api.security"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String SSL_ENABLED = "server.ssl.enabled";

    private static final String AUTH_BASIC_LOGIN = "/api/auth/basic/login";
    private static final String AUTH_BASIC_SIGNUP = "/api/auth/basic/registration/**";
    private static final String AUTH_BASIC_TOKEN = "/api/auth/basic/token";

    private static final String AUTH_SSO_LOGIN = "/api/auth/sso/login";

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    private Environment env;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GitLabProjectRepository gitLabProjectRepository;

    private static final String[] AUTH_WHITELIST = {
            "/favicon.ico",
            "/api/info/**",
            "/actuator/**",
            "/api/content/**",
            "/api/users/reset/**",
            "/api/mail",
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        boolean sslEnabled = Boolean.parseBoolean(env.getProperty(SSL_ENABLED, "false"));
        if (sslEnabled)
            http.requiresChannel().anyRequest().requiresSecure();
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(AUTH_BASIC_LOGIN).permitAll()
                .antMatchers(AUTH_BASIC_SIGNUP).permitAll()
                .antMatchers(AUTH_BASIC_TOKEN).permitAll()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .antMatchers(AUTH_SSO_LOGIN).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/orchestration/deployments/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/orchestration/deployments/**/state").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/orchestration/deployments/**/access").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/management/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/content/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/i18n/content/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/i18n/all/enabled").permitAll()
                .antMatchers(HttpMethod.GET, "/api/configuration/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/auth/sso").permitAll()
                .antMatchers(HttpMethod.POST, "/api/mail").permitAll()
                .antMatchers(HttpMethod.GET, "/api/mail/type").permitAll()
                .antMatchers("/api/users/reset/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/monitor/all").permitAll()
                .antMatchers("/api/orchestration/deployments/**/state").authenticated()
                .antMatchers("/api/orchestration/deployments/**/access").authenticated()
                .antMatchers("/api/orchestration/deployments/**").authenticated()
                .antMatchers("/api/management/**").authenticated()
                .antMatchers("/api/**").authenticated()
                .and()
                .addFilterBefore(
                        statelessAuthFilter(
                                new SkipPathRequestMatcher(
                                        new AntPathRequestMatcher[]{
                                                new AntPathRequestMatcher(AUTH_BASIC_LOGIN),
                                                new AntPathRequestMatcher(AUTH_BASIC_SIGNUP),
                                                new AntPathRequestMatcher(AUTH_BASIC_TOKEN),
                                                new AntPathRequestMatcher("/api/configuration/**", "GET"),
                                                new AntPathRequestMatcher("/api/auth/sso", "GET"),
                                                new AntPathRequestMatcher("/actuator/**"),
                                                new AntPathRequestMatcher("/favicon.ico"),
                                                new AntPathRequestMatcher(AUTH_SSO_LOGIN),
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
                                ),
                                null,
                                tokenAuthenticationService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        gitlabTokenFilter("/api/gitlab/webhooks/**",
                                null,
                                gitLabProjectRepository),
                        StatelessAuthenticationFilter.class
                );
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

        source.registerCorsConfiguration("/api/**", corsConfig);

        return new CorsFilter(source);
    }

    @Bean
    public JWTSettings jwtSettings() {
        return new JWTSettings();
    }
}

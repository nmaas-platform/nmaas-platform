package net.geant.nmaas;

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
	
	private static final  String SSL_ENABLED = "server.ssl.enabled";
	
	private static final  String AUTH_BASIC_LOGIN = "/api/auth/basic/login";
	private static final  String AUTH_BASIC_SIGNUP = "/api/auth/basic/registration/**";
	private static final  String AUTH_BASIC_TOKEN = "/api/auth/basic/token";

	private static final  String AUTH_SSO_LOGIN = "/api/auth/sso/login";

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private Environment env;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String[] AUTH_WHITELIST = {
			"/favicon.ico",
			"/v2/api-docs",
			"/swagger-resources",
			"/swagger-resources/**",
			"/configuration/ui",
			"/configuration/security",
			"/swagger-ui.html",
			"/api/info/**",
			"/actuator/health",
			"/actuator/prometheus",
			"/webjars/**",
			"/api/content/**",
			"/api/users/reset/**",
			"/api/mail"
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
				.antMatchers(HttpMethod.GET, "/api/management/shibboleth/").permitAll()
				.antMatchers(HttpMethod.POST, "/api/mail").permitAll()
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
											new AntPathRequestMatcher[] {
													new AntPathRequestMatcher(AUTH_BASIC_LOGIN),
													new AntPathRequestMatcher(AUTH_BASIC_SIGNUP),
													new AntPathRequestMatcher(AUTH_BASIC_TOKEN),
													new AntPathRequestMatcher("/api/configuration/**", "GET"),
													new AntPathRequestMatcher("/api/management/shibboleth/", "GET"),
													new AntPathRequestMatcher("/v2/api-docs"),
													new AntPathRequestMatcher("/swagger-resources"),
													new AntPathRequestMatcher("/swagger-resources/**"),
													new AntPathRequestMatcher("/configuration/ui"),
													new AntPathRequestMatcher("/configuration/security"),
													new AntPathRequestMatcher("/actuator/health"),
													new AntPathRequestMatcher("/actuator/prometheus"),
													new AntPathRequestMatcher("/swagger-ui.html"),
													new AntPathRequestMatcher("/webjars/**"),
													new AntPathRequestMatcher("/favicon.ico"),
													new AntPathRequestMatcher(AUTH_SSO_LOGIN),
													new AntPathRequestMatcher("/api/info/**"),
													new AntPathRequestMatcher("/api/dcns/notifications/**/status"),
													new AntPathRequestMatcher("/api/content/**"),
													new AntPathRequestMatcher("/api/users/reset/**"),
													new AntPathRequestMatcher("/api/mail"),
													new AntPathRequestMatcher("/api/monitor/all", "GET"),
													new AntPathRequestMatcher("/api/mail"),
													new AntPathRequestMatcher("/api/i18n/content/**", "GET"),
													new AntPathRequestMatcher("/api/i18n/all/enabled", "GET")
											}
											),
									null,
									tokenAuthenticationService),
							UsernamePasswordAuthenticationFilter.class
					);
	}

	private Filter statelessAuthFilter(RequestMatcher skipPaths, AuthenticationFailureHandler failureHandler, TokenAuthenticationService tokenService) {
		StatelessAuthenticationFilter filter = new StatelessAuthenticationFilter(skipPaths, tokenService);
		if(failureHandler != null)
			filter.setAuthenticationFailureHandler(failureHandler);
		return filter;
	}

	@Bean
	public CorsFilter corsFilter(){
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

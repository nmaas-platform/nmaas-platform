package net.geant.nmaas.configuration;

import net.geant.nmaas.portal.api.security.JWTSettings;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.RestAuthenticationEntryPoint;
import net.geant.nmaas.portal.api.security.SkipPathRequestMatcher;
import net.geant.nmaas.portal.api.security.StatelessAuthenticationFilter;
import net.geant.nmaas.portal.auth.basic.TokenAuthenticationService;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySource("classpath:application.properties")
@Order(Ordered.LOWEST_PRECEDENCE-100)
@ComponentScan(basePackages={"net.geant.nmaas.portal.api.security"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final  String SSL_ENABLED = "server.ssl.enabled";
	
	private static final  String AUTH_BASIC_LOGIN = "/api/auth/basic/login";
	private static final  String AUTH_BASIC_SIGNUP = "/api/auth/basic/registration/**";
	private static final  String AUTH_BASIC_TOKEN = "/api/auth/basic/token";

	private static final  String AUTH_SSO_LOGIN = "/api/auth/sso/login";

    private static final String ANSIBLE_NOTIFICATION_CLIENT_USERNAME_PROPERTY_NAME = "ansible.notification.client.username";
    private static final String ANSIBLE_NOTIFICATION_CLIENT_PASS_PROPERTY_NAME = "ansible.notification.client.password";
	private static final String APP_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME = "app.config.download.client.username";
	private static final String APP_CONFIG_DOWNLOAD_PASS_PROPERTY_NAME = "app.config.download.client.password";
	private static final String APP_COMPOSE_DOWNLOAD_USERNAME_PROPERTY_NAME = "app.compose.download.client.username";
	private static final String APP_COMPOSE_DOWNLOAD_PASS_PROPERTY_NAME = "app.compose.download.client.password";

	private static final String AUTH_ROLE_ANSIBLE_CLIENT = "ANSIBLE_CLIENT";
	private static final String AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT = "CONFIG_DOWNLOAD_CLIENT";
	private static final String AUTH_ROLE_COMPOSE_DOWNLOAD_CLIENT = "COMPOSE_DOWNLOAD_CLIENT";

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private Environment env;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "dcn_ansible".equals(p))) {
			auth.inMemoryAuthentication()
					.passwordEncoder(passwordEncoder())
					.withUser(env.getProperty(ANSIBLE_NOTIFICATION_CLIENT_USERNAME_PROPERTY_NAME))
					.password(passwordEncoder().encode(env.getProperty(ANSIBLE_NOTIFICATION_CLIENT_PASS_PROPERTY_NAME)))
					.roles(AUTH_ROLE_ANSIBLE_CLIENT);
		}
		if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "env_docker-compose".equals(p))) {
			auth.inMemoryAuthentication()
					.passwordEncoder(passwordEncoder())
					.withUser(env.getProperty(APP_COMPOSE_DOWNLOAD_USERNAME_PROPERTY_NAME))
					.password(passwordEncoder().encode(env.getProperty(APP_COMPOSE_DOWNLOAD_PASS_PROPERTY_NAME)))
					.roles(AUTH_ROLE_COMPOSE_DOWNLOAD_CLIENT);
			auth.inMemoryAuthentication()
					.passwordEncoder(passwordEncoder())
					.withUser(env.getProperty(APP_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME))
					.password(passwordEncoder().encode(env.getProperty(APP_CONFIG_DOWNLOAD_PASS_PROPERTY_NAME)))
					.roles(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT);
		}
	}

	private static final String[] AUTH_WHITELIST = {
			"/v2/api-docs",
			"/swagger-resources",
			"/swagger-resources/**",
			"/configuration/ui",
			"/configuration/security",
			"/swagger-ui.html",
			"/api/info/**",
			"/webjars/**",
			"/api/content/**",
			"/api/users/reset/**"
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
	            .antMatchers("/api/dcns/notifications/**/status").hasRole(AUTH_ROLE_ANSIBLE_CLIENT)
				.antMatchers("/api/configs/**").hasRole(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)
				.antMatchers("/api/dockercompose/files/**").hasRole(AUTH_ROLE_COMPOSE_DOWNLOAD_CLIENT)
	            .and().httpBasic()
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
				.antMatchers(HttpMethod.GET, "/api/configuration/**").permitAll()
				.antMatchers(HttpMethod.GET, "/api/management/shibboleth/").permitAll()
				.antMatchers("/api/users/reset/**").permitAll()
				.antMatchers("/api/**").authenticated()
				.antMatchers("/api/orchestration/deployments/**").authenticated()
				.antMatchers("/api/orchestration/deployments/**/state").authenticated()
				.antMatchers("/api/orchestration/deployments/**/access").authenticated()
				.antMatchers("/api/management/**").authenticated()
				.and()
					.addFilterBefore(statelessAuthFilter(
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
										new AntPathRequestMatcher("/swagger-ui.html"),
										new AntPathRequestMatcher("/webjars/**"),
										new AntPathRequestMatcher(AUTH_SSO_LOGIN),
										new AntPathRequestMatcher("/api/info/**"),
										new AntPathRequestMatcher("/api/dcns/notifications/**/status"),
										new AntPathRequestMatcher("/api/configs/**"),
										new AntPathRequestMatcher("/api/dockercompose/files/**"),
										new AntPathRequestMatcher("/api/content/**"),
										new AntPathRequestMatcher("/api/users/reset/**")
								}),
								null,//failureHandler, 
								tokenAuthenticationService),
						UsernamePasswordAuthenticationFilter.class);
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
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public JWTSettings jwtSettings() {
		return new JWTSettings();
	}

	@Bean
	@Autowired
	public TokenAuthenticationService tokenAuthenticationService(JWTTokenService jwtTokenService, UserRepository userRepository) {
		return new TokenAuthenticationService(jwtTokenService, userRepository);
	}
	
}

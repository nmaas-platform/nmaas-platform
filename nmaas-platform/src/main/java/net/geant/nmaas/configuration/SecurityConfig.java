package net.geant.nmaas.configuration;

import net.geant.nmaas.portal.api.security.*;
import net.geant.nmaas.portal.auth.basic.TokenAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
	
	private final static String SSL_ENABLED = "server.ssl.enabled";
	
	private final static String AUTH_BASIC_LOGIN = "/api/auth/basic/login";
	private final static String AUTH_BASIC_SIGNUP = "/api/auth/basic/registration/**";
	private final static String AUTH_BASIC_TOKEN = "/api/auth/basic/token";
	private final static String APP_LOGO = "/api/apps/{appId:[\\d+]}/logo";
	private final static String APP_SCREENSHOTS = "/api/apps/{appId:[\\d+]}/screenshots/**";

	private final static String AUTH_SSO_LOGIN = "/api/auth/sso/login";

    private static final String ANSIBLE_NOTIFICATION_CLIENT_USERNAME_PROPERTY_NAME = "ansible.notification.client.username";
    private static final String ANSIBLE_NOTIFICATION_CLIENT_PASSWORD_PROPERTY_NAME = "ansible.notification.client.password";
	private static final String APP_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME = "app.config.download.client.username";
	private static final String APP_CONFIG_DOWNLOAD_PASSWORD_PROPERTY_NAME = "app.config.download.client.password";
	private static final String APP_COMPOSE_DOWNLOAD_USERNAME_PROPERTY_NAME = "app.compose.download.client.username";
	private static final String APP_COMPOSE_DOWNLOAD_PASSWORD_PROPERTY_NAME = "app.compose.download.client.password";

	private static final String AUTH_ROLE_ANSIBLE_CLIENT = "ANSIBLE_CLIENT";
	private static final String AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT = "CONFIG_DOWNLOAD_CLIENT";
	private static final String AUTH_ROLE_COMPOSE_DOWNLOAD_CLIENT = "COMPOSE_DOWNLOAD_CLIENT";

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private Environment env;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.equals("dcn_ansible"))) {
			auth.inMemoryAuthentication()
					.withUser(env.getProperty(ANSIBLE_NOTIFICATION_CLIENT_USERNAME_PROPERTY_NAME))
					.password(env.getProperty(ANSIBLE_NOTIFICATION_CLIENT_PASSWORD_PROPERTY_NAME))
					.roles(AUTH_ROLE_ANSIBLE_CLIENT);
		}
		if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.equals("conf_download"))) {
			auth.inMemoryAuthentication()
					.withUser(env.getProperty(APP_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME))
					.password(env.getProperty(APP_CONFIG_DOWNLOAD_PASSWORD_PROPERTY_NAME))
					.roles(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT);
		}
		if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.equals("env_docker-compose"))) {
			auth.inMemoryAuthentication().withUser(env.getProperty(APP_COMPOSE_DOWNLOAD_USERNAME_PROPERTY_NAME))
					.password(env.getProperty(APP_COMPOSE_DOWNLOAD_PASSWORD_PROPERTY_NAME))
					.roles(AUTH_ROLE_COMPOSE_DOWNLOAD_CLIENT);
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
			"/webjars/**"
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
//				.antMatchers(HttpMethod.GET, APP_LOGO).permitAll()
//				.antMatchers(HttpMethod.GET, APP_SCREENSHOTS).permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/orchestration/deployments/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/orchestration/deployments/**/state").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/orchestration/deployments/**/access").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/management/**").permitAll()
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
										new AntPathRequestMatcher("/v2/api-docs"),
										new AntPathRequestMatcher("/swagger-resources"),
										new AntPathRequestMatcher("/swagger-resources/**"),
										new AntPathRequestMatcher("/configuration/ui"),
										new AntPathRequestMatcher("/configuration/security"),
										new AntPathRequestMatcher("/swagger-ui.html"),
										new AntPathRequestMatcher("/webjars/**"),
										new AntPathRequestMatcher(AUTH_SSO_LOGIN),
										new AntPathRequestMatcher("/api/info/**"),
//										new AntPathRequestMatcher(APP_LOGO, HttpMethod.GET.name()),
//										new AntPathRequestMatcher(APP_SCREENSHOTS, HttpMethod.GET.name()),
										new AntPathRequestMatcher("/api/dcns/notifications/**/status"),
										new AntPathRequestMatcher("/api/configs/**"),
										new AntPathRequestMatcher("/api/dockercompose/files/**")
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
	public TokenAuthenticationService tokenAuthenticationService(JWTTokenService jwtTokenService) {
		return new TokenAuthenticationService(jwtTokenService);
	}
	
}

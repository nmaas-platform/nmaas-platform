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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySource("classpath:application.properties")
@Order(Ordered.LOWEST_PRECEDENCE-100)
@ComponentScan(basePackages={"net.geant.nmaas.portal.api.security"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private final static String SSL_ENABLED="server.ssl.enabled";
	
	private final static String AUTH_BASIC_LOGIN = "/portal/api/auth/basic/login";
	private final static String AUTH_BASIC_SIGNUP = "/portal/api/auth/basic/signup";
	private final static String AUTH_BASIC_TOKEN = "/portal/api/auth/basic/token";
	private final static String APP_LOGO = "/portal/api/apps/{appId:[\\d+]}/logo";
	private final static String APP_SCREENSHOTS = "/portal/api/apps/{appId:[\\d+]}/screenshots/**";
	
    private static final String ANSIBLE_CLIENT_USERNAME_PROPERTY_NAME = "api.client.ansible.username";
    private static final String ANSIBLE_CLIENT_PASSWORD_PROPERTY_NAME = "api.client.ansible.password";
    private static final String NMAAS_TEST_CLIENT_USERNAME_PROPERTY_NAME = "api.client.nmaas.test.username";
    private static final String NMAAS_TEST_CLIENT_PASSWORD_PROPERTY_NAME = "api.client.nmaas.test.password";
	private static final String NMAAS_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME = "api.client.config.download.username";
	private static final String NMAAS_CONFIG_DOWNLOAD_PASSWORD_PROPERTY_NAME = "api.client.config.download.password";

	private static final String AUTH_ROLE_ANSIBLE_CLIENT = "ANSIBLE_CLIENT";
	private static final String AUTH_ROLE_NMAAS_TEST_CLIENT = "NMAAS_TEST_CLIENT";
	private static final String AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT = "CONFIG_DOWNLOAD_CLIENT";

	@Autowired
	TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private Environment env;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
				.withUser(env.getProperty(ANSIBLE_CLIENT_USERNAME_PROPERTY_NAME))
					.password(env.getProperty(ANSIBLE_CLIENT_PASSWORD_PROPERTY_NAME))
					.roles(AUTH_ROLE_ANSIBLE_CLIENT)
				.and()
				.withUser(env.getProperty(NMAAS_TEST_CLIENT_USERNAME_PROPERTY_NAME))
					.password(env.getProperty(NMAAS_TEST_CLIENT_PASSWORD_PROPERTY_NAME))
					.roles(AUTH_ROLE_NMAAS_TEST_CLIENT)
				.and()
				.withUser(env.getProperty(NMAAS_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME))
					.password(env.getProperty(NMAAS_CONFIG_DOWNLOAD_PASSWORD_PROPERTY_NAME))
					.roles(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT);
	}
	
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
	            .antMatchers("/platform/api/dcns/notifications/**/status").hasRole(AUTH_ROLE_ANSIBLE_CLIENT)
	            .antMatchers("/platform/api/dcns/**").hasRole(AUTH_ROLE_NMAAS_TEST_CLIENT)
	            .antMatchers("/platform/api/services/**").hasRole(AUTH_ROLE_NMAAS_TEST_CLIENT)
				.antMatchers("/platform/api/configs/**").hasRole(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)
				.antMatchers("/platform/api/dockercompose/files/**").hasRole(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)
	            .and().httpBasic()
			.and()
				.authorizeRequests()
				.antMatchers(AUTH_BASIC_LOGIN).permitAll()
				.antMatchers(AUTH_BASIC_SIGNUP).permitAll()
				.antMatchers(AUTH_BASIC_TOKEN).permitAll()
//				.antMatchers(HttpMethod.GET, APP_LOGO).permitAll()
//				.antMatchers(HttpMethod.GET, APP_SCREENSHOTS).permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/portal/api/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/platform/api/orchestration/deployments/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/platform/api/orchestration/deployments/**/state").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/platform/api/orchestration/deployments/**/access").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/platform/api/management/**").permitAll()
				.antMatchers("/portal/api/**").authenticated()
				.antMatchers("/platform/api/orchestration/deployments/**").authenticated()
				.antMatchers("/platform/api/orchestration/deployments/**/state").authenticated()
				.antMatchers("/platform/api/orchestration/deployments/**/access").authenticated()
				.antMatchers("/platform/api/management/**").authenticated()
			.and()
				.addFilterBefore(statelessAuthFilter(
						new SkipPathRequestMatcher(
								new AntPathRequestMatcher[] { 
										new AntPathRequestMatcher(AUTH_BASIC_LOGIN), 
										new AntPathRequestMatcher(AUTH_BASIC_SIGNUP),
										new AntPathRequestMatcher(AUTH_BASIC_TOKEN),
//										new AntPathRequestMatcher(APP_LOGO, HttpMethod.GET.name()),
//										new AntPathRequestMatcher(APP_SCREENSHOTS, HttpMethod.GET.name()),
										new AntPathRequestMatcher("/platform/api/dcns/notifications/**"),
										new AntPathRequestMatcher("/platform/api/dcns/**"),
										new AntPathRequestMatcher("/platform/api/services/**"),
										new AntPathRequestMatcher("/platform/api/configs/**"),
										new AntPathRequestMatcher("/platform/api/dockercompose/files/**")
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
	public FilterRegistrationBean corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration corsConfig = new CorsConfiguration();
		
		//TODO: customize CORS through properties. Currently CORS is enabled for /api
		corsConfig.addAllowedOrigin("*");
		corsConfig.addAllowedHeader("*");
		corsConfig.addAllowedMethod("*");
		
		source.registerCorsConfiguration("/portal/api/**", corsConfig);
		source.registerCorsConfiguration("/platform/api/**", corsConfig);

		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setFilter(new CorsFilter(source));
		bean.setOrder(0);
		return bean;
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

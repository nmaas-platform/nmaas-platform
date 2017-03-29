package net.geant.nmaas.configuration;

import net.geant.nmaas.portal.api.security.*;
import net.geant.nmaas.portal.auth.basic.TokenAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
@Order(Ordered.LOWEST_PRECEDENCE-100)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private final static String SSL_ENABLED="server.ssl.enabled";
	
	private final static String AUTH_BASIC_LOGIN = "/portal/api/auth/basic/login";
	private final static String AUTH_BASIC_SIGNUP = "/portal/api/auth/basic/signup";
	private final static String AUTH_BASIC_TOKEN = "/portal/api/auth/basic/token";

    private static final String ANSIBLE_CLIENT_USERNAME_PROPERTY_NAME = "api.client.ansible.username";
    private static final String ANSIBLE_CLIENT_PASSWORD_PROPERTY_NAME = "api.client.ansible.password";
    private static final String NMAAS_TEST_CLIENT_USERNAME_PROPERTY_NAME = "api.client.nmaas.test.username";
    private static final String NMAAS_TEST_CLIENT_PASSWORD_PROPERTY_NAME = "api.client.nmaas.test.password";
	private static final String NMAAS_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME = "api.client.config.download.username";
	private static final String NMAAS_CONFIG_DOWNLOAD_PASSWORD_PROPERTY_NAME = "api.client.config.download.password";

    public static final String AUTH_ROLE_ANSIBLE_CLIENT = "ANSIBLE_CLIENT";
    public static final String AUTH_ROLE_NMAAS_TEST_CLIENT = "NMAAS_TEST_CLIENT";
	public static final String AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT = "CONFIG_DOWNLOAD_CLIENT";

//	@Autowired
//	AuthenticationManager authenticationManager;

	@Autowired
	TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private Environment env;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("user1").password("user1Pass").authorities("ROLE_USER");

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
				.antMatchers("/platform/api/orchestration/**").hasRole(AUTH_ROLE_NMAAS_TEST_CLIENT)
				.antMatchers("/platform/api/configs/**").hasRole(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)
				.antMatchers("/platform/api/management/**").hasRole(AUTH_ROLE_NMAAS_TEST_CLIENT)
	            .and().httpBasic()
			.and()
				.authorizeRequests()
				.antMatchers(AUTH_BASIC_LOGIN).permitAll()
				.antMatchers(AUTH_BASIC_SIGNUP).permitAll()
				.antMatchers(AUTH_BASIC_TOKEN).permitAll()
				.antMatchers("/portal/api/**").authenticated()
			.and()
//				.addFilterBefore(statelessLoginFilter("/platform/**",	inMemoryUserDetailsService()), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(statelessAuthFilter(
						new SkipPathRequestMatcher(
								new String[] { 	AUTH_BASIC_LOGIN, 
												AUTH_BASIC_SIGNUP, 
												AUTH_BASIC_TOKEN,
												"/platform/**"}), 
								null,//failureHandler, 
								tokenAuthenticationService), 
						UsernamePasswordAuthenticationFilter.class);		
	}

//	private InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryConfigurer() {
//		return new InMemoryUserDetailsManagerConfigurer<>();
//	}
//
//	private UserDetailsService inMemoryUserDetailsService() {
//		return inMemoryConfigurer().withUser(env.getProperty(ANSIBLE_CLIENT_USERNAME_PROPERTY_NAME))
//					      .password(env.getProperty(ANSIBLE_CLIENT_PASSWORD_PROPERTY_NAME))
//					      .roles(AUTH_ROLE_ANSIBLE_CLIENT)
//					      .and()
//					      .withUser(env.getProperty(NMAAS_TEST_CLIENT_USERNAME_PROPERTY_NAME))
//					      .password(env.getProperty(NMAAS_TEST_CLIENT_PASSWORD_PROPERTY_NAME))
//					      .roles(AUTH_ROLE_NMAAS_TEST_CLIENT)
//						  .and()
//						  .withUser(env.getProperty(NMAAS_CONFIG_DOWNLOAD_USERNAME_PROPERTY_NAME))
//						  .password(env.getProperty(NMAAS_CONFIG_DOWNLOAD_PASSWORD_PROPERTY_NAME))
//						  .roles(AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)
//					      .and()
//					      .getUserDetailsService();
//	}
	
	private Filter statelessAuthFilter(RequestMatcher skipPaths, AuthenticationFailureHandler failureHandler, TokenAuthenticationService tokenService) {
		StatelessAuthenticationFilter filter = new StatelessAuthenticationFilter(skipPaths, tokenService);
		if(failureHandler != null)
			filter.setAuthenticationFailureHandler(failureHandler);
//		filter.setAuthenticationManager(authenticationManager);
		return filter;
	}

//	private Filter statelessLoginFilter(String processUrl, UserDetailsService userDetailsService) {
//		StatelessLoginFilter filter = new StatelessLoginFilter(processUrl, userDetailsService);
//		filter.setAuthenticationManager(authenticationManager);
//		return filter;
//	}

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

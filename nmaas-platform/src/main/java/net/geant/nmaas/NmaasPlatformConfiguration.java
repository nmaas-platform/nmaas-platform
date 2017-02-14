package net.geant.nmaas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class NmaasPlatformConfiguration extends WebSecurityConfigurerAdapter {

    private static final String ANSIBLE_CLIENT_USERNAME_PROPERTY_NAME = "api.client.ansible.username";
    private static final String ANSIBLE_CLIENT_PASSWORD_PROPERTY_NAME = "api.client.ansible.password";
    private static final String NMAAS_TEST_CLIENT_USERNAME_PROPERTY_NAME = "api.client.nmaas.test.username";
    private static final String NMAAS_TEST_CLIENT_PASSWORD_PROPERTY_NAME = "api.client.nmaas.test.password";

    public static final String AUTH_ROLE_ANSIBLE_CLIENT = "ANSIBLE_CLIENT";
    public static final String AUTH_ROLE_NMAAS_TEST_CLIENT = "NMAAS_TEST_CLIENT";

    @Autowired
    private Environment env;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    protected void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(env.getProperty(ANSIBLE_CLIENT_USERNAME_PROPERTY_NAME))
                .password(env.getProperty(ANSIBLE_CLIENT_PASSWORD_PROPERTY_NAME))
                .roles(AUTH_ROLE_ANSIBLE_CLIENT)
                .and()
                .withUser(env.getProperty(NMAAS_TEST_CLIENT_USERNAME_PROPERTY_NAME))
                .password(env.getProperty(NMAAS_TEST_CLIENT_PASSWORD_PROPERTY_NAME))
                .roles(AUTH_ROLE_NMAAS_TEST_CLIENT);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/dcns/notifications/**/status").hasRole(AUTH_ROLE_ANSIBLE_CLIENT)
                .antMatchers("/api/dcns/**").hasRole(AUTH_ROLE_NMAAS_TEST_CLIENT)
                .antMatchers("/api/services/**").hasRole(AUTH_ROLE_NMAAS_TEST_CLIENT)
                .and().httpBasic().authenticationEntryPoint(restAuthenticationEntryPoint)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

}

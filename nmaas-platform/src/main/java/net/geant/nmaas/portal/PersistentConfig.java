package net.geant.nmaas.portal;

import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.security.core.Authentication;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"net.geant.nmaas.portal.persistent.repositories"})
@EnableJpaAuditing(auditorAwareRef="auditorProvider")
@PropertySource("classpath:db.properties")
@ComponentScan("net.geant.nmaas.portal.persistent.repositories")
@EntityScan("net.geant.nmaas.portal.persistent.entity")
public class PersistentConfig {
//	public final static String DRIVER="db.driver";
//	public final static String URL="db.url";
//	public final static String USERNAME="db.username";
//	public final static String PASSWORD="db.password";

	@Autowired
	private Environment env;
	
	@Bean
	@ConfigurationProperties("db")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	AuditorAware<User> auditorProvider() {
		return new AuditorAware<User>() {
			@Autowired
			UserRepository userRepo;
			
			@Override
			public User getCurrentAuditor() {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if(auth == null)
					throw new UsernameNotFoundException("Authentication object not found.");
				
				String username = auth.getName();
				if(username == null)
					throw new UsernameNotFoundException("Username is null.");
				
				Optional<User> user = userRepo.findByUsername(username);
				if(!user.isPresent())
					throw new UsernameNotFoundException("User " + username + " not found.");
				
				return user.get();
			}
			
		};
	}
	
}

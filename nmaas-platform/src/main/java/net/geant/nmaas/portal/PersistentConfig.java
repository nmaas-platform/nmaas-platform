package net.geant.nmaas.portal;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"net.geant.nmaas"})
@EnableJpaAuditing(auditorAwareRef="auditorProvider")
@PropertySource("classpath:application.properties")
@ComponentScan("net.geant.nmaas")
@EntityScan("net.geant.nmaas")
public class PersistentConfig {

	@Bean
	@Profile("db_memory")
	@ConfigurationProperties("db.in_memory")
	public DataSource inMemoryDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	@Profile("db_standalone")
	@ConfigurationProperties("db.standalone")
	public DataSource standaloneDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	AuditorAware<User> auditorProvider() {
		return new AuditorAware<User>() {
			@Autowired
			UserRepository userRepo;
			
			@Override
			@Transactional(propagation=Propagation.REQUIRES_NEW)
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

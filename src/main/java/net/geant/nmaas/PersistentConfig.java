package net.geant.nmaas;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef="auditorProvider")
public class PersistentConfig {

	@Bean
	AuditorAware<User> auditorProvider() {
		return new AuditorAware<User>() {
			@Autowired
			UserRepository userRepo;
			
			@Override
			@Transactional(propagation=Propagation.REQUIRES_NEW)
			public Optional<User> getCurrentAuditor() {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth == null) {
					throw new UsernameNotFoundException("Authentication object not found.");
				}

				String username = auth.getName();
				if (username == null) {
					throw new UsernameNotFoundException("Username is null.");
				}

				User user = userRepo.findByUsername(username).orElseThrow(()
						-> new UsernameNotFoundException("User " + username + " not found."));

				return Optional.of(user);
			}
		};
	}
}

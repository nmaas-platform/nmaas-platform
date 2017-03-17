package net.geant.nmaas.portal;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.Filter;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.CharacterEncodingFilter;

import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.FileStorageService;
import net.geant.nmaas.portal.service.LocalFileStorageService;

@Configuration
@ComponentScan(basePackages={"net.geant.nmaas.portal.service"})
public class PortalConfig {

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Bean
	public InitializingBean insertDefaultUsers() {
		return new InitializingBean() {
			
			@Autowired
			private UserRepository userRepository;
			
			// For demo purposes
			@Override
			public void afterPropertiesSet() {
				addUser("admin", "admin", new Role[] { Role.ADMIN, Role.MANAGER });
				addUser("manager", "manager", Role.MANAGER);
				addUser("user", "user", Role.USER);
			}

			private void addUser(String username, String password, Role role) {								
				User user = new User(username, passwordEncoder.encode(password), role);
				userRepository.save(user);
			}
			
			private void addUser(String username, String password, Role[] roles) {								
				User user = new User(username, passwordEncoder.encode(password), new ArrayList<Role>(Arrays.asList(roles)));
				userRepository.save(user);
			}
			
		};
	}
	
	@Bean
	public FileStorageService localFileStorageService() {
		return new LocalFileStorageService();
	}
	
	
	@Bean
	public Filter characterEncodingFilter() {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		return characterEncodingFilter;
	}
}

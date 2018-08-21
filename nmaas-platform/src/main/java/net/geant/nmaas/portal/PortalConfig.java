package net.geant.nmaas.portal;

import java.util.Optional;

import javax.servlet.Filter;

import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;

import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.FileStorageService;
import net.geant.nmaas.portal.service.impl.LocalFileStorageService;

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
			
			@Autowired
			private DomainService domains;



			@Override
			@Transactional
			public void afterPropertiesSet() throws ProcessingException {
				domains.createGlobalDomain();				
				
				Optional<User> admin = userRepository.findByUsername("admin");
				if(!admin.isPresent())
					addUser("admin", "admin", Role.ROLE_SUPERADMIN);

			}

			private void addUser(String username, String password, Role role) {								
				User user = new User(username, true, passwordEncoder.encode(password), domains.getGlobalDomain().get(), role, true);
				userRepository.save(user);
			}
						
		};
	}

	@Bean
	public InitializingBean insertDefaultTos(){
		return new InitializingBean() {

			@Autowired
			private ContentRepository contentRepository;


			@Override
			public void afterPropertiesSet() throws Exception {
				Optional<Content> tos = contentRepository.findByName("tos");
				if(!tos.isPresent()){
					addTos("tos", "Terms of use", "Lorem ipsum dolor sit amet, consectetur");
				}
			}

			private void addTos(String name, String title, String content){
				Content cnt = new Content(name, title, content);
				contentRepository.save(cnt);
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

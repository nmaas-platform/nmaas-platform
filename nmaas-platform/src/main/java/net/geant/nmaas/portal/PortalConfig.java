package net.geant.nmaas.portal;

import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.FileStorageService;
import net.geant.nmaas.portal.service.impl.LocalFileStorageService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.Filter;
import java.io.IOException;
import java.util.Optional;

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
				if(!admin.isPresent()) {
					addUser("admin", "admin", Role.ROLE_SUPERADMIN);
				}
			}

			private void addUser(String username, String password, Role role) {								
				User user = new User(username, true, passwordEncoder.encode(password), domains.getGlobalDomain().get(), role, true, true);
				userRepository.save(user);
			}
						
		};
	}

	@Bean
	public InitializingBean insertDefaultTos(){
		return new InitializingBean() {
			@Autowired
			private ContentRepository contentRepository;
			@Autowired
			private ResourceLoader resourceLoader;
			@Override
			@Transactional
			public void afterPropertiesSet() {
				Optional<Content> defaultTermsOfUse = contentRepository.findByName("tos");
				if(!defaultTermsOfUse.isPresent()){
					try {
						addContentToDatabase("tos", "Terms of use", readContent("classpath:tos.txt"));
					}catch (IOException e){
						throw new ProcessingException(e);
					}
				}
				Optional<Content> defaultPrivacyPolicy = contentRepository.findByName("pp");
				if(!defaultPrivacyPolicy.isPresent()){
					try {
						addContentToDatabase("pp", "Privacy Policy", readContent("classpath:pp.txt"));
					}catch (IOException e){
						throw new ProcessingException(e);
					}
				}
			}
			private String readContent(String file) throws IOException {
				return new String(IOUtils.toString(resourceLoader.getResource(file).getInputStream(), "utf-8"));
			}
			private void addContentToDatabase(String name, String title, String content){
				Content newContent = new Content(name, title, content);
				contentRepository.save(newContent);
			}
		};
	}

	@Bean
	public InitializingBean addConfigurationProperties(){
		return new InitializingBean() {
			@Autowired
			ConfigurationManager configurationManager;

			@Override
			@Transactional
			public void afterPropertiesSet() throws Exception {
				try {
					net.geant.nmaas.portal.persistent.entity.Configuration configuration = configurationManager.getConfiguration();
					if(configuration.isMaintenance())
						configuration.setMaintenance(false);
					if(configuration.isSsoLoginAllowed())
						configuration.setSsoLoginAllowed(true);

				} catch(IllegalStateException e){
					configurationManager.deleteAllConfigurations();
					configurationManager.addConfiguration(new net.geant.nmaas.portal.persistent.entity.Configuration(false, false));
				}
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

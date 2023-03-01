package net.geant.nmaas.portal;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages={"net.geant.nmaas.portal.service"})
@Log4j2
public class PortalConfig {

	private final PasswordEncoder passwordEncoder;

	@Bean
	public InitializingBean insertDefaultUsers() {
		return new InitializingBean() {
			
			@Autowired
			private UserRepository userRepository;
			
			@Autowired
			private DomainService domains;

			@Value("${admin.password}")
			String adminPassword;

			@Value("${admin.email}")
			String adminEmail;

			@Value("${portal.config.defaultLanguage}")
			private String defaultLanguage = "en";

			@Override
			@Transactional
			public void afterPropertiesSet() {
				domains.createGlobalDomain();				
				
				Optional<User> admin = userRepository.findByUsername("admin");
				if(admin.isEmpty()) {
					addUser("admin", adminPassword, adminEmail, Role.ROLE_SYSTEM_ADMIN);
				}
			}

			private void addUser(String username, String password, String email, Role role) {
				Optional<Domain> globalDomain = domains.getGlobalDomain();
				if(globalDomain.isPresent()) {
					User user = new User(username, true, passwordEncoder.encode(password), globalDomain.get(), role, true, true);
					user.setEmail(email);
					user.setSelectedLanguage(this.defaultLanguage);
					userRepository.save(user);
				}
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
				if(defaultTermsOfUse.isEmpty()){
					try {
						addContentToDatabase("tos", "Terms of use", readContent("classpath:tos.txt"));
					}catch (IOException err) {
						throw new ProcessingException(err.getMessage());
					}
				}
				Optional<Content> defaultPrivacyPolicy = contentRepository.findByName("pp");
				if(defaultPrivacyPolicy.isEmpty()){
					try {
						addContentToDatabase("pp", "Privacy Policy", readContent("classpath:pp.txt"));
					} catch (IOException err) {
						throw new ProcessingException(err.getMessage());
					}
				}
			}

			private String readContent(String file) throws IOException {
				return new String(resourceLoader.getResource(file).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			}

			private void addContentToDatabase(String name, String title, String content){
				Content newContent = new Content(name, title, content);
				contentRepository.save(newContent);
			}
		};
	}

	@Bean
	public InitializingBean saveDefaultPortalConfiguration() {
		return new InitializingBean() {

			@Value("${portal.config.maintenance:false}")
			private boolean maintenance;

			@Value("${portal.config.ssoLoginAllowed:false}")
			private boolean ssoLoginAllowed;

			@Value("${portal.config.defaultLanguage}")
			private String defaultLanguage = "en";

			@Value("${portal.config.testInstance:false}")
			private boolean testInstance;

			@Value("${portal.config.sendAppInstanceFailureEmails:false}")
			private boolean sendAppInstanceFailureEmails;

			@Value("${portal.config.appInstanceFailureEmailList}")
			private String appInstanceFailureEmailList;

			@Value("${portal.config.showDomainRegistrationSelector:true}")
			private boolean showDomainRegistrationSelector;

			@Autowired
			private ConfigurationManager configurationManager;

			@Override
			public void afterPropertiesSet() throws Exception {
				ConfigurationView configurationView = ConfigurationView.builder()
						.maintenance(this.maintenance)
						.ssoLoginAllowed(this.ssoLoginAllowed)
						.defaultLanguage(this.defaultLanguage)
						.testInstance(this.testInstance)
						.sendAppInstanceFailureEmails(this.sendAppInstanceFailureEmails)
						.appInstanceFailureEmailList(Arrays.asList(this.appInstanceFailureEmailList.split(";")))
						.registrationDomainSelectionEnabled(this.showDomainRegistrationSelector)
						.build();
				try {
					this.configurationManager.setConfiguration(configurationView);
				} catch (OnlyOneConfigurationSupportedException e) {
					log.debug("Portal configuration already exists. Skipping initialization.");
				}
			}
		};
	}
}

package net.geant.nmaas.portal;

import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.model.MonitorEntryView;
import net.geant.nmaas.monitor.scheduling.ScheduleManager;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Content;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.ContentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import org.apache.commons.io.IOUtils;
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
@ComponentScan(basePackages={"net.geant.nmaas.portal.service"})
public class PortalConfig {

	private PasswordEncoder passwordEncoder;

	@Autowired
	public PortalConfig(PasswordEncoder passwordEncoder){
		this.passwordEncoder = passwordEncoder;
	}
	
	@Bean
	public InitializingBean insertDefaultUsers() {
		return new InitializingBean() {
			
			@Autowired
			private UserRepository userRepository;
			
			@Autowired
			private DomainService domains;

			@Autowired
			private ConfigurationManager configurationManager;

			@Value("${admin.password}")
			String adminPassword;

			@Value("${admin.email}")
			String adminEmail;

			@Override
			@Transactional
			public void afterPropertiesSet() {
				domains.createGlobalDomain();				
				
				Optional<User> admin = userRepository.findByUsername("admin");
				if(!admin.isPresent()) {
					addUser("admin", adminPassword, adminEmail, Role.ROLE_SYSTEM_ADMIN);
				}
			}

			private void addUser(String username, String password, String email, Role role) {
				Optional<Domain> globalDomain = domains.getGlobalDomain();
				if(globalDomain.isPresent()) {
					User user = new User(username, true, passwordEncoder.encode(password), globalDomain.get(), role, true, true);
					user.setEmail(email);
					user.setSelectedLanguage(configurationManager.getConfiguration().getDefaultLanguage());
					userRepository.save(user);
				}
			}
						
		};
	}

	@Bean
	public InitializingBean insertDefaultMonitoringJobs(){
		return new InitializingBean() {

			@Autowired
			private ScheduleManager scheduleManager;

			@Autowired
			private MonitorManager monitorManager;

			@Override
			@Transactional
			public void afterPropertiesSet() {
				Arrays.stream(ServiceType.values())
						.filter(service -> !this.scheduleManager.jobExists(service.toString())) // if job does not exist
						.forEach(service ->{
							monitorManager.deleteMonitorEntry(service.toString()); // delete entry if exists
							MonitorEntryView monitorEntry = service.getDefaultMonitorEntry();
							this.monitorManager.createMonitorEntry(monitorEntry); // create new default entry
							this.scheduleManager.createJob(monitorEntry);
						});
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
					}catch (IOException err){
						throw new ProcessingException(err.getMessage());
					}
				}
				Optional<Content> defaultPrivacyPolicy = contentRepository.findByName("pp");
				if(!defaultPrivacyPolicy.isPresent()){
					try {
						addContentToDatabase("pp", "Privacy Policy", readContent("classpath:pp.txt"));
					}catch (IOException err){
						throw new ProcessingException(err.getMessage());
					}
				}
			}

			private String readContent(String file) throws IOException {
				return IOUtils.toString(resourceLoader.getResource(file).getInputStream(), StandardCharsets.UTF_8);
			}

			private void addContentToDatabase(String name, String title, String content){
				Content newContent = new Content(name, title, content);
				contentRepository.save(newContent);
			}
		};
	}
}
package net.geant.nmaas;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@DependsOn("portalConfiguration")
@Slf4j
public class DefaultUsersInit implements InitializingBean {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final DomainService domainService;
    private final String adminPassword;
    private final String adminEmail;
    private final String defaultLanguage;

    @Autowired
    public DefaultUsersInit(PasswordEncoder passwordEncoder,
                            UserRepository userRepository,
                            DomainService domainService,
                            @Value("${admin.password}") String adminPassword,
                            @Value("${admin.email}") String adminEmail,
                            @Value("${portal.config.defaultLanguage:en}") String defaultLanguage) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.domainService = domainService;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
        log.info("[Init] Configuring default users");
        domainService.createGlobalDomain();
        Optional<User> admin = userRepository.findByUsername("admin");
        if (admin.isEmpty()) {
            log.debug("Adding admin user");
            addDefaultAdmin(adminPassword, adminEmail);
        } else {
            log.debug("Admin user already exists");
        }
    }

    private void addDefaultAdmin(String password, String email) {
        Optional<Domain> globalDomain = domainService.getGlobalDomain();
        if (globalDomain.isPresent()) {
            User user = new User("admin", true, passwordEncoder.encode(password), globalDomain.get(), Role.ROLE_SYSTEM_ADMIN, true, true);
            user.setEmail(email);
            user.setSelectedLanguage(defaultLanguage);
            userRepository.save(user);
        }
    }

}

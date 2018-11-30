package net.geant.nmaas.portal.auth.basic;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.domain.Domain;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.model.ConfirmationEmail;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ContentService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/basic/registration")
@Log4j2
public class RegistrationController {

	private UserService usersService;

	private DomainService domains;

	private PasswordEncoder passwordEncoder;

	private ModelMapper modelMapper;

	private NotificationService notificationService;

	private ContentService contentService;

	@Autowired
	public RegistrationController(UserService usersService,
								  DomainService domains,
								  PasswordEncoder passwordEncoder,
								  ModelMapper modelMapper,
								  NotificationService notificationService,
								  ContentService contentService){
		this.usersService = usersService;
		this.domains = domains;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper = modelMapper;
		this.notificationService = notificationService;
		this.contentService = contentService;
	}
	
	@PostMapping
	@Transactional
    @ResponseStatus(HttpStatus.CREATED)
	public void signup(@RequestBody final Registration registration) {
		if(registration == null
				|| StringUtils.isEmpty(registration.getUsername())
				|| StringUtils.isEmpty(registration.getPassword())) {
			throw new SignupException("REGISTRATION.INVALID_CREDENTIALS_MESSAGE");
		}
		User newUser = null;
		try {
			newUser = usersService.register(registration.getUsername(), domains.getGlobalDomain().orElseThrow(MissingElementException::new));
			if (newUser == null) {
				throw new SignupException("REGISTRATION.UNABLE_TO_REGISTER_MESSAGE");
			}
			if (!registration.getTermsOfUseAccepted()) {
				throw new SignupException("REGISTRATION.TERMS_NOT_ACCEPTED_MESSAGE");
			}
			if (!registration.getPrivacyPolicyAccepted()) {
				throw new SignupException("REGISTRATION.PRIVACY_POLICY_NOT_ACCEPTED_MESSAGE");
			}
		} catch (ObjectAlreadyExistsException e) {
			throw new SignupException("REGISTRATION.USER_ALREADY_EXISTS_MESSAGE");
		} catch (MissingElementException e) {
			throw new SignupException("REGISTRATION.DOMAIN_NOT_FOUND_MESSAGE");
		} catch (Exception e) {
			throw new SignupException("REGISTRATION.UNABLE_TO_FETCH_CONTENT_MESSAGE");
		}
		
		newUser.setPassword(passwordEncoder.encode(registration.getPassword()));
		newUser.setEmail(registration.getEmail());
		newUser.setFirstname(registration.getFirstname());
		newUser.setLastname(registration.getLastname());
		newUser.setEnabled(false);
		newUser.setTermsOfUseAccepted(registration.getTermsOfUseAccepted());
		newUser.setPrivacyPolicyAccepted(registration.getPrivacyPolicyAccepted());

		try {
			usersService.update(newUser);
            log.info(String.format("The user with user name - %s, first name - %s, last name - %s, email - %s have signed up with domain id - %s.",
                    registration.getUsername(),
                    registration.getFirstname(),
                    registration.getLastname(),
                    registration.getEmail(),
                    registration.getDomainId()));

            ConfirmationEmail confirmationEmail = ConfirmationEmail
                    .builder()
                    .firstName(newUser.getFirstname())
                    .lastName(newUser.getLastname())
                    .toEmail(usersService.findAllUsersEmailWithAdminRole())
                    .userName(newUser.getUsername())
                    .subject("NMaaS: New account registration request")
                    .templateName("admin-notification")
                    .build();
			notificationService.sendEmail(confirmationEmail);

			if(registration.getDomainId() != null) {
                domains.addMemberRole(registration.getDomainId(), newUser.getId(), Role.ROLE_GUEST);
            }
		} catch (ObjectNotFoundException e) {
			throw new SignupException("Domain not found."); 
		} catch (ProcessingException e) {
			throw new SignupException("Unable to update newly registered user.");
		} 
	}
	
	@GetMapping("/domains")
	@Transactional(readOnly=true)
	public List<Domain> getDomains() {
		Optional<Domain> globalDomain = domains.getGlobalDomain().map(domain -> modelMapper.map(domain, Domain.class));
		final Long globalDomainId;
		
		if(globalDomain.isPresent())
			globalDomainId = globalDomain.get().getId();
		else
			globalDomainId = null;
		
		return domains.getDomains().stream()
						.map(domain -> modelMapper.map(domain, Domain.class))
						.filter(domain -> !domain.getId().equals(globalDomainId))
						.collect(Collectors.toList());
		
	}
}
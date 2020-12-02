package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.domain.DomainBase;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.utils.captcha.ValidateCaptcha;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth/basic/registration")
@Log4j2
public class RegistrationController {

	private UserService usersService;

	private DomainService domains;

	private ModelMapper modelMapper;

	private ApplicationEventPublisher eventPublisher;
	
	@PostMapping
	@ValidateCaptcha
    @ResponseStatus(HttpStatus.CREATED)
	public void signup(@RequestBody final Registration registration, @RequestParam String token) {
		if(registration == null
				|| StringUtils.isEmpty(registration.getUsername())
				|| StringUtils.isEmpty(registration.getPassword())
				|| StringUtils.isEmpty(registration.getEmail())) {
			throw new SignupException("Invalid credentials");
		}

		// explicit null values handling
		Boolean termsOfUseAccepted = registration.getTermsOfUseAccepted();
		if(Boolean.FALSE.equals(termsOfUseAccepted)){
			throw new SignupException("Terms of Use were not accepted");
		}
		Boolean privacyPolicyAccepted = registration.getPrivacyPolicyAccepted();
		if(Boolean.FALSE.equals(privacyPolicyAccepted)){
			throw new SignupException("Privacy policy was not accepted");
		}
		Domain domain = null;
		if(registration.getDomainId() != null){
			domain = domains.findDomain(registration.getDomainId()).orElseThrow(()-> new SignupException("Domain not found"));
		}
		Domain globalDomain = domains.getGlobalDomain().orElseThrow(MissingElementException::new);
		try {
			User newUser = usersService.register(registration, globalDomain, domain);
			log.info(String.format("A new user [%s] with first name [%s], last name [%s] and email [%s] have signed up with domain [%s].",
					registration.getUsername(),
					registration.getFirstname(),
					registration.getLastname(),
					registration.getEmail(),
					registration.getDomainId()));
			this.sendMail(newUser);
			if(registration.getDomainId() != null) {
				domains.addMemberRole(registration.getDomainId(), newUser.getId(), Role.ROLE_GUEST);
			}
		} catch (ObjectAlreadyExistsException e){
			throw new SignupException("User already exists");
		} catch (MissingElementException e){
			throw new SignupException("Domain not found");
		}
	}
	
	@GetMapping("/domains")
	@Transactional(readOnly=true)
	public List<DomainBase> getDomains() {
		final Long globalDomainId = domains.getGlobalDomain().orElseThrow(MissingElementException::new).getId();
		
		return domains.getDomains().stream()
						.map(domain -> modelMapper.map(domain, DomainBase.class))
						.filter(domain -> !domain.getId().equals(globalDomainId))
						.collect(Collectors.toList());
		
	}

	private void sendMail(User user){
		MailAttributes mailAttributes = MailAttributes
				.builder()
				.otherAttributes(ImmutableMap.of("newUser", user.getUsername()))
				.mailType(MailType.REGISTRATION)
				.build();
		this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
	}
}
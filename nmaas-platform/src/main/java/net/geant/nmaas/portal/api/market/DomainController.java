package net.geant.nmaas.portal.api.market;

import static com.google.common.base.Preconditions.checkArgument;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployedEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnRemoveActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/api/domains")
public class DomainController extends AppBaseController {

	private UserService userService;
	
	private DomainService domainService;

	private ApplicationEventPublisher eventPublisher;

	private static final String UNABLE_TO_CHANGE_DOMAIN_ID = "Unable to change domain id";
	private static final String DOMAIN_NOT_FOUND = "Domain not found.";

	@Autowired
	public DomainController(UserService userService, DomainService domainService, ApplicationEventPublisher eventPublisher){
		this.userService = userService;
		this.domainService = domainService;
		this.eventPublisher = eventPublisher;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<DomainView> getDomains() {
		return domainService.getDomains().stream().map(d -> modelMapper.map(d, DomainView.class)).collect(Collectors.toList());
	}
	
	@GetMapping("/my")
	@Transactional(readOnly = true)
	public List<DomainView> getMyDomains(@NotNull Principal principal) {
		User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new ProcessingException("User not found."));
					
		try {
			return domainService.getUserDomains(user.getId()).stream().map(d -> modelMapper.map(d, DomainView.class)).collect(Collectors.toList());
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}
	}
	
	@PostMapping
	@Transactional
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	public Id createDomain(@RequestBody(required=true) DomainRequest domainRequest) {
		if(domainService.existsDomain(domainRequest.getName())) 
			throw new ProcessingException("Domain already exists.");
		
		Domain domain;
		try {
			domain = domainService.createDomain(domainRequest);
			this.domainService.storeDcnInfo(domain.getCodename(), domain.getDomainDcnDetails().getDcnDeploymentType());

			if(domain.getDomainDcnDetails().isDcnConfigured()){
				this.eventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, domain.getCodename(), DcnDeploymentState.DEPLOYED));
				this.eventPublisher.publishEvent(new DcnDeployedEvent(this, domain.getCodename()));
			}

			return new Id(domain.getId());
		} catch (InvalidDomainException e) {
			throw new ProcessingException(e.getMessage());
		}
	}
	
	@PutMapping("/{domainId}")
	@Transactional
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	public Id updateDomain(@PathVariable Long domainId, @RequestBody(required=true) DomainView domainUpdate) {
		if(!domainId.equals(domainUpdate.getId()))
			throw new ProcessingException(UNABLE_TO_CHANGE_DOMAIN_ID);
		
		Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND));
		
		domain.setName(domainUpdate.getName());
		domain.setActive(domainUpdate.isActive());
		domain.getDomainTechDetails().setKubernetesNamespace(domainUpdate.getDomainTechDetails().getKubernetesNamespace());
		domain.getDomainTechDetails().setKubernetesStorageClass(domainUpdate.getDomainTechDetails().getKubernetesStorageClass());
		domain.getDomainDcnDetails().setDcnDeploymentType(domainUpdate.getDomainDcnDetails().getDcnDeploymentType());
		if(StringUtils.isEmpty(domainUpdate.getDomainTechDetails().getExternalServiceDomain())){
			domain.getDomainTechDetails().setExternalServiceDomain(domainUpdate.getDomainTechDetails().getExternalServiceDomain());
		} else if(!domainUpdate.getDomainTechDetails().getExternalServiceDomain().equalsIgnoreCase(domain.getDomainTechDetails().getExternalServiceDomain())){
			checkArgument(!domainService.existsDomainByExternalServiceDomain(domainUpdate.getDomainTechDetails().getExternalServiceDomain()), "External service domain is not unique");
			domain.getDomainTechDetails().setExternalServiceDomain(domainUpdate.getDomainTechDetails().getExternalServiceDomain());
		}
		domainService.updateDomain(domain);
		domainService.updateDcnInfo(domain.getCodename(), domain.getDomainDcnDetails().getDcnDeploymentType());
		
		return new Id(domainId);
	}

	@PatchMapping("/{domainId}")
	@Transactional
	@PreAuthorize("hasRole('ROLE_OPERATOR')")
	public Id updateDomainTechDetails(@PathVariable Long domainId, @RequestBody DomainView domainUpdate) {
		if(!domainId.equals(domainUpdate.getId())){
			throw new ProcessingException(UNABLE_TO_CHANGE_DOMAIN_ID);
		}
		Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND));
		domain.getDomainTechDetails().setKubernetesNamespace(domainUpdate.getDomainTechDetails().getKubernetesNamespace());
		domain.getDomainTechDetails().setKubernetesStorageClass(domainUpdate.getDomainTechDetails().getKubernetesStorageClass());
		domain.getDomainDcnDetails().setDcnDeploymentType(domainUpdate.getDomainDcnDetails().getDcnDeploymentType());

		domainService.updateDomain(domain);
		domainService.updateDcnInfo(domain.getCodename(), domainUpdate.getDomainDcnDetails().getDcnDeploymentType());


		return new Id(domainId);
	}

	@PatchMapping("/{domainId}/state")
	@Transactional
	@PreAuthorize("hasRole('ROLE_OPERATOR') || hasRole('ROLE_SYSTEM_ADMIN')")
	public void updateDomainState(@PathVariable Long domainId, @RequestParam boolean active){
		this.domainService.changeDomainState(domainId, active);
	}

	@PatchMapping("/{domainId}/dcn")
	@Transactional
	@PreAuthorize("hasRole('ROLE_OPERATOR') || hasRole('ROLE_SYSTEM_ADMIN')")
	public Id updateDcnConfiguredFlag(@PathVariable Long domainId, @RequestParam(value = "configured") boolean dcnConfigured) {
		Domain domain = domainService.changeDcnConfiguredFlag(domainId, dcnConfigured);
		if(domain.getDomainDcnDetails().isDcnConfigured()){
			this.eventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, domain.getCodename(), DcnDeploymentState.DEPLOYED));
			this.eventPublisher.publishEvent(new DcnDeployedEvent(this, domain.getCodename()));
		} else{
			this.eventPublisher.publishEvent(new DcnRemoveActionEvent(this, domain.getCodename()));
		}

		return new Id(domainId);
	}
	
	@DeleteMapping("/{domainId}")
	@Transactional
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	public void deleteDomain(@PathVariable Long domainId) {		
		if(!domainService.removeDomain(domainId))
			throw new MissingElementException("Unable to delete domain");
	}

	@GetMapping("/{domainId}")
	@Transactional(readOnly = true)	
	@PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
	public DomainView getDomain(@PathVariable Long domainId) {
		Domain domain = domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND));
		return modelMapper.map(domain, DomainView.class);
	}
	
	
}

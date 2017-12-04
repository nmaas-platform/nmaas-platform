package net.geant.nmaas.portal.api.market;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.ProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.Domain;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.service.DomainService;

@RestController
@RequestMapping("/portal/api/domains")
public class DomainController extends AppBaseController {

	@Autowired
	DomainService domainService;
	
	@GetMapping
	@Transactional(readOnly = true)
	public List<Domain> getDomains() {
		return domainService.getDomains().stream().map(d -> modelMapper.map(d, Domain.class)).collect(Collectors.toList());
	}
	
	@PostMapping
	@Transactional
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	public Id createDomain(@RequestBody(required=true) String name) {
		if(domainService.findDomain(name) != null)
			throw new ProcessingException("Domain already exists.");
		
		net.geant.nmaas.portal.persistent.entity.Domain domain = domainService.createDomain(name);
		
		return new Id(domain.getId());
	}
	
	@DeleteMapping("/{domainId}")
	@Transactional
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	public void deleteDomain(@PathVariable Long domainId) throws MissingElementException {		
		if(!domainService.removeDomain(domainId))
			throw new MissingElementException("Unable to delete domain");
	}

	@GetMapping("/{domainId}")
	@Transactional(readOnly = true)	
	public Domain getDomain(@PathVariable Long domainId) throws MissingElementException {	
		net.geant.nmaas.portal.persistent.entity.Domain domain = domainService.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found.");
		return modelMapper.map(domain, Domain.class);
	}
	
	
}

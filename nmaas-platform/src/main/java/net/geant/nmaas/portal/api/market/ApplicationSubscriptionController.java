package net.geant.nmaas.portal.api.market;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.ApplicationBrief;
import net.geant.nmaas.portal.api.domain.ApplicationSubscription;
import net.geant.nmaas.portal.api.domain.ApplicationSubscriptionBase;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
public class ApplicationSubscriptionController extends AppBaseController {
	
	@Autowired
	ApplicationSubscriptionService appSubscriptions;
	
	@PostMapping
	@PreAuthorize("hasPermission(#appSubscription.domainId, 'domain', 'OWNER')")
	@Transactional
	@ResponseStatus(HttpStatus.CREATED)
	public void subscribe(@RequestBody ApplicationSubscriptionBase appSubscription) throws ProcessingException {
		try {
			net.geant.nmaas.portal.persistent.entity.ApplicationSubscription appSub = appSubscriptions.subscribe(appSubscription.getApplicationId(), appSubscription.getDomainId(), true);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to subscribe. " + e.getMessage());
		}
			
	}

	@PostMapping("/request")
	@PreAuthorize("hasPermission(#appSubscription.domainId, 'domain', 'ANY')")
	@Transactional
	public void subscribeRequest(@RequestBody ApplicationSubscriptionBase appSubscription) throws ProcessingException {
		try {
			net.geant.nmaas.portal.persistent.entity.ApplicationSubscription appSub = appSubscriptions.subscribe(appSubscription.getApplicationId(), appSubscription.getDomainId(), false);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to subscribe. " + e.getMessage());
		}		
	}
	
	
	@DeleteMapping("/apps/{appId}/domains/{domainId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	@Transactional
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void unsubscribe(@PathVariable Long domainId, @PathVariable Long appId) throws ProcessingException {
		try {
			boolean result = appSubscriptions.unsubscribe(appId, domainId);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to unsubscribe. " + e.getMessage());
		}
	}
	
	@GetMapping("/apps/{appId}/domains/{domainId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
	@Transactional(readOnly=true)
	public ApplicationSubscription getSubscription(@PathVariable Long domainId, @PathVariable Long appId) throws MissingElementException {
		return appSubscriptions.getSubscription(appId, domainId).map(appSub -> modelMapper.map(appSub, ApplicationSubscription.class))
				.orElseThrow(() -> new MissingElementException("Subscription not found"));
	}
	
	@GetMapping
	@Transactional(readOnly=true)	
	public List<ApplicationSubscriptionBase> getAllSubscriptions() {
		return appSubscriptions.getSubscriptions().stream().filter(appSub->!appSub.isDeleted())
				.map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
	}
	
	
	@GetMapping("/domains/{domainId}")
	@Transactional(readOnly=true)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
	public List<ApplicationSubscriptionBase> getDomainSubscriptions(@PathVariable Long domainId) {
		return appSubscriptions.getSubscriptionsBy(domainId, null).stream()
				.map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
	}
	
	@GetMapping("/domains/{domainId}/apps")
	@Transactional(readOnly=true)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
	public List<ApplicationBrief> getDomainSubscribedApplications(@PathVariable Long domainId) {
		return appSubscriptions.getSubscribedApplications(domainId).stream().map(app -> modelMapper.map(app, ApplicationBrief.class)).collect(Collectors.toList());
	}
	
	@GetMapping("/apps")
	@Transactional(readOnly=true)
	public List<ApplicationBrief> getSubscribedApplications() {
		return appSubscriptions.getSubscribedApplications().stream().map(app -> modelMapper.map(app, ApplicationBrief.class)).collect(Collectors.toList());		
	}
	
	@GetMapping("/apps/{appId}")
	@Transactional(readOnly=true)
	public List<ApplicationSubscriptionBase> getApplicationSubscriptions(@PathVariable Long appId) {
		return appSubscriptions.getSubscriptionsBy(null, appId).stream()
				.map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
	}
	
}

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.ApplicationSubscriptionBase;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;

@RestController
public class ApplicationSubscriptionController extends AppBaseController {
	
	@Autowired
	ApplicationSubscriptionService appSubscriptions;
	
	@PostMapping("/portal/api/apps/subscribe")
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

	@PostMapping("/portal/api/apps/subscribe/request")
	@PreAuthorize("hasPermission(#appSubscription.domainId, 'domain', 'ANY')")
	@Transactional
	public void subscribeRequest(@RequestBody ApplicationSubscriptionBase appSubscription) throws ProcessingException {
		try {
			net.geant.nmaas.portal.persistent.entity.ApplicationSubscription appSub = appSubscriptions.subscribe(appSubscription.getApplicationId(), appSubscription.getDomainId(), false);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to subscribe. " + e.getMessage());
		}		
	}
	
	
	@DeleteMapping({"/portal/api/domains/{domainId}/apps/{appId}", "/portal/api/apps/{appId}/domains/{domainId}"})
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
	
	@GetMapping("/portal/api/apps/subscriptions")
	@Transactional(readOnly=true)
	public List<ApplicationSubscriptionBase> getAllSubscriptions() {
		return appSubscriptions.getSubscriptions().stream()
				.map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
	}
	
	
	@GetMapping("/portal/api/domains/{domainId}/subscriptions")
	@Transactional(readOnly=true)
	public List<ApplicationSubscriptionBase> getDomainSubscriptions(@PathVariable Long domainId) {
		return appSubscriptions.getSubscriptionsBy(domainId, null).stream()
				.map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
	}
	
	@GetMapping("/portal/api/apps/{appId}/subscriptions")
	@Transactional(readOnly=true)
	public List<ApplicationSubscriptionBase> getApplicationSubscriptions(@PathVariable Long appId) {
		return appSubscriptions.getSubscriptionsBy(null, appId).stream()
				.map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
	}
	
}

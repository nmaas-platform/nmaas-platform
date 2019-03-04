package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableMap;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.portal.api.domain.ApplicationStateChangeRequest;
import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ApplicationBriefView;
import net.geant.nmaas.portal.api.domain.Id;

@RestController
@AllArgsConstructor
@RequestMapping("/api/apps")
public class ApplicationController extends AppBaseController {

	private ApplicationEventPublisher eventPublisher;

	@GetMapping
	@Transactional
	public List<ApplicationBriefView> getApplications() {
		return applications.findAll().stream()
				.filter(app -> app.getState().equals(ApplicationState.ACTIVE) || app.getState().equals(ApplicationState.DISABLED))
				.map(app -> modelMapper.map(app, ApplicationBriefView.class))
				.collect(Collectors.toList());
	}

	@GetMapping("/all")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public List<ApplicationBriefView> getAllApplications(){
		return applications.findAll().stream()
				.map(app -> modelMapper.map(app, ApplicationBriefView.class))
				.collect(Collectors.toList());
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody(required=true) ApplicationView appRequest, Principal principal) {
		Application app = applications.create(appRequest.getName(), appRequest.getVersion(), principal.getName());
		applications.setMissingProperties(appRequest);
		modelMapper.map(appRequest, app);
		applications.update(app);
		this.sendMail(app, new ApplicationStateChangeRequest(app.getState(), ""));
		return new Id(app.getId());
	}

	@PatchMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplication(@RequestBody ApplicationView appRequest){
		applications.setMissingProperties(appRequest);
		applications.update(modelMapper.map(appRequest, Application.class));
	}

	@DeleteMapping(value="/{appId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteApplication(@PathVariable(value = "appId") long appId){
	    applications.delete(appId);
    }

	@GetMapping(value="/{appId}")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	@Transactional
	public ApplicationView getApplication(@PathVariable(value = "appId", required=true) Long id) {
		Application app = getApp(id);
		return modelMapper.map(app, ApplicationView.class);
	}

	@PatchMapping(value = "/state/{appId}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	@Transactional
	public void changeApplicationState(@PathVariable(value = "appId") long appId, @RequestBody ApplicationStateChangeRequest stateChangeRequest){
		Application app = getApp(appId);
		applications.changeApplicationState(app, stateChangeRequest.getState());
		this.sendMail(app, stateChangeRequest);
	}

	private void sendMail(Application app, ApplicationStateChangeRequest stateChangeRequest){
		MailAttributes mailAttributes = MailAttributes.builder()
				.mailType(stateChangeRequest.getState().getMailType())
				.otherAttributes(ImmutableMap.of("app_name", app.getName(), "app_version", app.getVersion(), "reason", stateChangeRequest.getReason() == null? "": stateChangeRequest.getReason()))
				.build();
		if(!stateChangeRequest.getState().equals(ApplicationState.NEW)){
			User owner = modelMapper.map(users.findByUsername(app.getOwner()).orElseThrow(() -> new IllegalArgumentException("Owner not found")), User.class);
			mailAttributes.setAddressees(Collections.singletonList(owner));
		}
		this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
	}

}

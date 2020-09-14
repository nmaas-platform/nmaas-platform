package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableMap;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api/apps")
@Log4j2
public class ApplicationController extends AppBaseController {

	private final ApplicationEventPublisher eventPublisher;

	/*
	 * Application Base Part
	 */

	@GetMapping("/base")
	@Transactional
	public List<ApplicationBaseView> getAllActiveOrDisabledApplicationBase() {
		return appBaseService.findAllActiveOrDisabledApps().stream()
				.map(app -> modelMapper.map(app, ApplicationBaseView.class))
				.collect(Collectors.toList());
	}

	@GetMapping("/base/all")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public List<ApplicationBaseView> getAllApplicationBase(){
		return appBaseService.findAll().stream()
				.map(app -> modelMapper.map(app, ApplicationBaseView.class))
				.collect(Collectors.toList());
	}

	@GetMapping(value = "/base/{appId}")
	@Transactional
	public ApplicationBaseView getBaseApplication(@PathVariable(value = "appId") Long id) {
		ApplicationBase app = appBaseService.getBaseApp(id);
		return modelMapper.map(app, ApplicationBaseView.class);
	}

	@PatchMapping(value = "/base")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplicationBase(@RequestBody ApplicationBaseView appRequest){
		appBaseService.updateApplicationBase(appRequest);
	}

	/*
	 * Application part
	 */
	
	@PostMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody @Valid ApplicationMassiveView appRequest, Principal principal) {
		Application app = applications.create(appRequest, principal.getName());
		appRequest.setId(app.getId());
		ApplicationBase appBase = appBaseService.createApplicationOrAddNewVersion(appRequest);
		this.sendMail(app, new ApplicationStateChangeRequest(app.getState(), ""));
		return new Id(appBase.getId());
	}

	@PatchMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplication(@RequestBody ApplicationMassiveView appRequest){
		applications.setMissingProperties(appRequest, appRequest.getId());
		applications.update(modelMapper.map(appRequest, Application.class));
	}

	@GetMapping(value = "/{appName}/latest")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public ApplicationMassiveView getLatestAppVersion(@PathVariable String appName){
		return modelMapper.map(applications.findApplicationLatestVersion(appName), ApplicationMassiveView.class);
	}

	@GetMapping(value="/{appId}")
	@Transactional
	public ApplicationMassiveView getApplication(@PathVariable(value = "appId") Long id) {
		Application app = getApp(id);
		return modelMapper.map(app, ApplicationMassiveView.class);
	}

	/**
	 * use this method to add new ApplicationVersion and Application for existing ApplicationBaseEntity
	 * @param view - application entity view
	 * @param principal - security object (used to retrieve creator)
	 */
	@PostMapping(value = "/version")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void addApplicationVersion(@RequestBody @Valid ApplicationView view, Principal principal) {

		// validate
		// application base with given name must exist
		ApplicationBase base = appBaseService.findByName(view.getName());
		// specified version for this application base must not exist
		boolean hasVersion = base.getVersions().stream().anyMatch(v -> v.getVersion().equals(view.getVersion()));
		// application specified name and version must not exist
		boolean exists = applications.exists(view.getName(), view.getVersion());
		if(hasVersion || exists) {
			throw new ObjectAlreadyExistsException("App version already exists");
		}

		// create application stub to avoid problems with circular dependencies
		// see application -> app config spec -> config file template -> application (id) :)
		Long appId = this.applications.createOrUpdate(new Application(view.getName(), view.getVersion(), principal.getName()));

		// create application entity & set properties
		Application application = modelMapper.map(view, Application.class);
		application.setId(appId);
		application.setOwner(principal.getName());
		application.setState(ApplicationState.NEW);
		application.setCreationDate(LocalDateTime.now());
		this.applications.createOrUpdate(application);

		// create, add and persist new application version
		ApplicationVersion version = new ApplicationVersion(application.getVersion(), ApplicationState.NEW, appId);
		base.getVersions().add(version);
		appBaseService.updateApplicationBase(base);
	}

	/*
	 * both
	 */

	/**
	 *
	 * @param appId application id (not an ApplicationBase or ApplicationVersion id)
	 * @param stateChangeRequest request object
	 */
	@PatchMapping(value = "/state/{appId}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	@Transactional
	public void changeApplicationState(@PathVariable(value = "appId") long appId, @RequestBody ApplicationStateChangeRequest stateChangeRequest){
		Application app = getApp(appId);
		applications.changeApplicationState(app, stateChangeRequest.getState());
		appBaseService.updateApplicationVersionState(app.getName(), app.getVersion(), stateChangeRequest.getState());
		this.sendMail(app, stateChangeRequest);
	}

	/**
	 * Deletes application entity, labels application version as deleted
	 * @param appId application id (not an ApplicationBase or ApplicationVersion id)
	 */
	@DeleteMapping(value="/{appId}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void deleteApplication(@PathVariable(value = "appId") long appId){
		Application app = getApp(appId);
		applications.delete(appId);
		appBaseService.updateApplicationVersionState(app.getName(), app.getOwner(), ApplicationState.DELETED);
	}

	/*
	 * Utilities
	 */

	private void sendMail(Application app, ApplicationStateChangeRequest stateChangeRequest){
		MailAttributes mailAttributes = MailAttributes.builder()
				.mailType(stateChangeRequest.getState().getMailType())
				.otherAttributes(ImmutableMap.of("app_name", app.getName(), "app_version", app.getVersion(), "reason", stateChangeRequest.getReason() == null? "": stateChangeRequest.getReason()))
				.build();
		if(!stateChangeRequest.getState().equals(ApplicationState.NEW)){
			UserView owner = modelMapper.map(users.findByUsername(app.getOwner()).orElseThrow(() -> new IllegalArgumentException("Owner not found")), UserView.class);
			mailAttributes.setAddressees(Collections.singletonList(owner));
		}
		this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
	}

}

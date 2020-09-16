package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableMap;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.repositories.RatingRepository;
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

	private final RatingRepository ratingRepository;

	/*
	 * Application Base Part
	 */

	@GetMapping("/base")
	@Transactional
	public List<ApplicationBaseView> getAllActiveOrDisabledApplicationBase() {
		return appBaseService.findAllActiveOrDisabledApps().stream()
				.map(app -> modelMapper.map(app, ApplicationBaseView.class))
				.map(this::setAppRating)
				.collect(Collectors.toList());
	}

	@GetMapping("/base/all")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public List<ApplicationBaseView> getAllApplicationBase(){
		return appBaseService.findAll().stream()
				.map(app -> modelMapper.map(app, ApplicationBaseView.class))
				.map(this::setAppRating)
				.collect(Collectors.toList());
	}

	private ApplicationBaseView setAppRating(ApplicationBaseView baseView) {
		Integer[] rating = this.ratingRepository.getApplicationRating(baseView.getId());
		baseView.setRate(this.createAppRateView(rating));
		return baseView;
	}

	private AppRateView createAppRateView(Integer[] rating) {
		return new AppRateView(
				Arrays.stream(rating).mapToInt(Integer::intValue).average().orElse(0.0),
				Arrays.stream(rating).collect(Collectors.groupingBy(s -> s, Collectors.counting()))
		);
	}

	@GetMapping(value = "/base/{id}")
	@Transactional
	public ApplicationBaseView getApplicationBase(@PathVariable Long id) {
		ApplicationBaseView app = modelMapper.map(appBaseService.getBaseApp(id), ApplicationBaseView.class);
		return this.setAppRating(app);
	}

	@PatchMapping(value = "/base")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplicationBase(@RequestBody ApplicationBaseView baseView){
		appBaseService.update(modelMapper.map(baseView, ApplicationBase.class));
	}

	/*
	 * Application part
	 */
	
	@PostMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody @Valid ApplicationDTO request, Principal principal) {
		ApplicationBase base = this.appBaseService.create(modelMapper.map(request.getApplicationBase(), ApplicationBase.class));
		log.info(base.getDescriptions().get(0).getFullDescription());
		this.addApplicationVersion(request.getApplication(), principal);
		return new Id(base.getId());
	}

	@GetMapping(value = "/{name}/latest")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public ApplicationDTO getLatestAppVersion(@PathVariable String name) {
		ApplicationBase base = this.appBaseService.findByName(name);
		Application application = this.applicationService.findApplicationLatestVersion(name);
		return new ApplicationDTO(
				modelMapper.map(base, ApplicationBaseView.class),
				modelMapper.map(application, ApplicationView.class)
		);
	}

	@GetMapping(value="/{id}")
	@Transactional
	public ApplicationDTO getApplicationDTO(@PathVariable Long id) {
		Application app = getApp(id);
		ApplicationBase base = this.appBaseService.findByName(app.getName());
		return new ApplicationDTO(
				modelMapper.map(base, ApplicationBaseView.class),
				modelMapper.map(app, ApplicationView.class)
		);
	}

	@GetMapping(value="/version/{id}")
	@Transactional
	public ApplicationView getApplication(@PathVariable Long id) {
		Application app = getApp(id);
		return modelMapper.map(app, ApplicationView.class);
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
		boolean exists = applicationService.exists(view.getName(), view.getVersion());
		if(hasVersion || exists) {
			log.error("Cannot add application version, object already exists");
			throw new ObjectAlreadyExistsException("App version already exists");
		}

		// create application stub to avoid problems with circular dependencies
		// see application -> app config spec -> config file template -> application (id) :)
		Application temp = this.applicationService.create(new Application(view.getName(), view.getVersion(), principal.getName()));
		Long appId = temp.getId();

		// create application entity & set properties
		Application application = modelMapper.map(view, Application.class);
		application.setId(appId);
		application.setOwner(principal.getName());
		application.setState(ApplicationState.NEW);
		application.setCreationDate(LocalDateTime.now());
		this.applicationService.setMissingProperties(application, appId);
		this.applicationService.update(application);

		// create, add and persist new application version
		ApplicationVersion version = new ApplicationVersion(application.getVersion(), ApplicationState.NEW, appId);
		base.getVersions().add(version);
		appBaseService.update(base);

		this.sendMail(application, new ApplicationStateChangeRequest(application.getState(), ""));
	}

	@PatchMapping(value = "/version")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void updateApplicationVersion(@RequestBody @Valid ApplicationView view){

		// check if id exists
		if(view.getId() == null) {
			log.error("ID is not present in Application update");
			throw new ProcessingException("Cannot update application without id");
		}

		// application with specified name and version must exist
		Optional<Application> optId = applicationService.findApplication(view.getId());
		Optional<Application> optNameVersion = applicationService.findApplication(view.getName(), view.getVersion());

		if(!optId.isPresent() || !optNameVersion.isPresent()) {
			log.error("Requested application does not exist");
			throw new MissingElementException("Application does not exist");
		}

		if(!optId.get().equals(optNameVersion.get())) {
			log.error("Retrieved different applications using id and name&version, update aborted");
			throw new ProcessingException("You cannot change application name, version and id");
		}

		// application base with given name must exist
		ApplicationBase base = appBaseService.findByName(view.getName());

		// you cannot really change version label
		Optional<ApplicationVersion> version = base.getVersions().stream()
				.filter(v -> v.getVersion().equals(view.getVersion()) && v.getAppVersionId().equals(view.getId()))
				.findFirst();

		if (!version.isPresent()) {
			log.error("Application version cannot be updated (no matching versions available in ApplicationBase)");
			throw new ProcessingException("Cannot update application version");
		}

		Application application = modelMapper.map(view, Application.class);
		// rewrite creation date
		application.setCreationDate(optId.get().getCreationDate());
		applicationService.update(application);
	}

	/*
	 * both
	 */

	/**
	 *
	 * @param id application id (not an ApplicationBase or ApplicationVersion id)
	 * @param stateChangeRequest request object
	 */
	@PatchMapping(value = "/state/{id}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
	@Transactional
	public void changeApplicationState(@PathVariable long id, @RequestBody ApplicationStateChangeRequest stateChangeRequest){
		Application app = getApp(id);
		applicationService.changeApplicationState(app, stateChangeRequest.getState());
		appBaseService.updateApplicationVersionState(app.getName(), app.getVersion(), stateChangeRequest.getState());
		this.sendMail(app, stateChangeRequest);
	}

	/**
	 * Deletes application entity, labels application version as deleted
	 * @param id application id (not an ApplicationBase or ApplicationVersion id)
	 */
	@DeleteMapping(value="/{id}")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public void deleteApplication(@PathVariable long id){
		Application app = getApp(id);
		applicationService.delete(id);
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
			UserView owner = modelMapper.map(userService.findByUsername(app.getOwner()).orElseThrow(() -> new IllegalArgumentException("Owner not found")), UserView.class);
			mailAttributes.setAddressees(Collections.singletonList(owner));
		}
		this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
	}

}

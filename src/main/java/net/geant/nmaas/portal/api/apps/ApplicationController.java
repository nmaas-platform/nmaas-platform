package net.geant.nmaas.portal.api.apps;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.api.dto.applications.AppRateDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseView;
import net.geant.nmaas.api.dto.applications.ApplicationBaseViewS;
import net.geant.nmaas.api.dto.applications.ApplicationStateChangeRequest;
import net.geant.nmaas.api.dto.applications.ApplicationStateDto;
import net.geant.nmaas.api.dto.applications.ApplicationView;
import net.geant.nmaas.api.dto.users.UserDto;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.PortalException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.RatingRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.portal.service.impl.ApplicationServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/apps")
@Slf4j
@Tag(name = "Applications", description = "Operations related to applications")
public class ApplicationController extends AppBaseController {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class ApplicationDto {
        @Valid
        private ApplicationBaseView applicationBase;
        @Valid
        private ApplicationView application;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class ApplicationDtoVersionList {
        private ApplicationBaseView applicationBase;
        private List<ApplicationView> applications;
    }

    private final ApplicationEventPublisher eventPublisher;
    private final RatingRepository ratingRepository;
    private final ApplicationInstanceService applicationInstanceService;
    private final ApplicationSubscriptionService applicationSubscriptionService;

    @Autowired
    public ApplicationController(ModelMapper modelMapper, ApplicationService applicationService, ApplicationBaseService applicationBaseService,
                                 UserService userService, ApplicationEventPublisher eventPublisher, RatingRepository ratingRepository,
                                 ApplicationInstanceService applicationInstanceService, ApplicationSubscriptionService applicationSubscriptionService) {
        super(modelMapper, userService, applicationService, applicationBaseService);
        this.eventPublisher = eventPublisher;
        this.ratingRepository = ratingRepository;
        this.applicationInstanceService = applicationInstanceService;
        this.applicationSubscriptionService = applicationSubscriptionService;
    }

    /*
     * Application Base Part
     */

    @GetMapping("/base")
    @Transactional
    public List<ApplicationBaseViewS> getAllActiveApplicationBase() {
        return applicationBaseService.findAllActiveAppsSmall().stream()
                .map(this::setAppRating)
                .toList();
    }

    @GetMapping("/base/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public List<ApplicationBaseView> getAllApplicationBaseBasedOnRole(Principal principal) {
        // user with Tool Manager role should only see applications he owns
        boolean isSystemAdmin = this.getUser(principal.getName()).getRoles().stream()
                .anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN));

        return applicationBaseService.findAll().stream()
                // system admin should see all the applications
                .filter(app -> isSystemAdmin || app.getOwner().equals(principal.getName()))
                .map(app -> modelMapper.map(app, ApplicationBaseView.class))
                .map(this::setAppRating)
                .toList();
    }

    private ApplicationBaseView setAppRating(ApplicationBaseView baseView) {
        Integer[] rating = ratingRepository.getApplicationRating(baseView.getId());
        baseView.setRate(createAppRateView(rating));
        return baseView;
    }

    private ApplicationBaseViewS setAppRating(ApplicationBaseViewS baseView) {
        Integer[] rating = ratingRepository.getApplicationRating(baseView.getId());
        baseView.setRate(createAppRateView(rating));
        return baseView;
    }

    private static AppRateDto createAppRateView(Integer[] rating) {
        return new AppRateDto(
                Arrays.stream(rating).mapToInt(Integer::intValue).average().orElse(0.0),
                Arrays.stream(rating).collect(Collectors.groupingBy(s -> s, Collectors.counting()))
        );
    }

    @GetMapping(value = "/base/{id}")
    @Transactional
    public ApplicationBaseView getApplicationBase(@PathVariable Long id) {
        ApplicationBaseView app = modelMapper.map(applicationBaseService.getBaseApp(id), ApplicationBaseView.class);
        return this.setAppRating(app);
    }

    @PatchMapping(value = "/base")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void updateApplicationBase(@RequestBody ApplicationBaseView baseView, Principal principal) {
        // only system admin and owner can update application base
        this.applicationBaseOwnerCheck(baseView.getName(), principal);
        applicationBaseService.update(modelMapper.map(baseView, ApplicationBase.class));
    }

    @PatchMapping(value = "/base/{id}/owner/{owner}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void updateApplicationBaseOwner(@PathVariable Long id, @PathVariable String owner, Principal principal) {
        log.info("Updating owner of application {} to {}", id, owner);
        this.applicationBaseOwnerCheck(id, principal);
        applicationBaseService.updateOwner(id, owner);
    }

    @DeleteMapping(value = "/base/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteApplicationBase(@PathVariable Long id, Principal principal) {
        ApplicationBase base = applicationBaseService.getBaseApp(id);
        // only system admin and owner can update application base
        this.applicationBaseOwnerCheck(base.getName(), principal);
        for (ApplicationVersion appVersion : base.getVersions()) {
            Application app = getApp(appVersion.getAppVersionId());
            if (app.getState() != ApplicationState.DELETED) {
                throw new ProcessingException("Can't delete " + base.getName() + " application base since version " + app.getVersion() + " is not deleted");
            }
        }
        applicationSubscriptionService.unsubscribeAll(base);
        applicationBaseService.deleteAppBase(base);
    }

    @GetMapping(value = "/base/name/{name}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public ApplicationBaseView getApplicationBase(@PathVariable String name) {
        ApplicationBaseView app = modelMapper.map(applicationBaseService.findByName(name), ApplicationBaseView.class);
        return this.setAppRating(app);
    }

    /*
     * Application part
     */

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public Id addApplication(@RequestBody @Valid ApplicationController.ApplicationDto request, Principal principal) {
        ApplicationBaseView creationRequest = request.getApplicationBase();
        creationRequest.setOwner(principal.getName());
        // create new application base
        ApplicationBase base = applicationBaseService.create(modelMapper.map(creationRequest, ApplicationBase.class));

        this.addApplicationVersion(request.getApplication(), principal);

        return new Id(base.getId());
    }

    @GetMapping(value = "/{name}/latest")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public ApplicationDto getLatestAppVersion(@PathVariable String name) {
        ApplicationBase base = applicationBaseService.findByName(name);
        Application application = applicationService.findApplicationLatestVersion(name);
        return new ApplicationDto(
                modelMapper.map(base, ApplicationBaseView.class),
                modelMapper.map(application, ApplicationView.class)
        );
    }

    @GetMapping(value = "/{name}/version/{version}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public ApplicationDto getApplicationByNameAndVersion(@PathVariable String name, @PathVariable String version) {
        ApplicationBase base = applicationBaseService.findByName(name);

        Optional<Application> application = applicationService.findApplication(name, version);

        if (application.isPresent()) {
            return new ApplicationDto(
                    modelMapper.map(base, ApplicationBaseView.class),
                    modelMapper.map(application.get(), ApplicationView.class)
            );
        } else {
            throw new MissingElementException("Application " + name + " version " + version + " not found");
        }

    }

    @GetMapping(value = "/{id}")
    @Transactional
    public ApplicationDto getApplicationDTO(@PathVariable Long id) {
        Application app = getApp(id);
        ApplicationBase base = applicationBaseService.findByName(app.getName());
        return new ApplicationDto(
                modelMapper.map(base, ApplicationBaseView.class),
                modelMapper.map(app, ApplicationView.class)
        );
    }

    @GetMapping(value = "/base/allversions/{id}")
    @Transactional
    public ApplicationDtoVersionList getApplicationDTOWithAllVersions(@PathVariable Long id) {
        ApplicationBase base = applicationBaseService.getBaseApp(id);
        List<Application> versionList = applicationService.findAll().stream()
                .filter(app -> app.getName().equalsIgnoreCase(base.getName()))
                .toList();
        return new ApplicationDtoVersionList(
                modelMapper.map(base, ApplicationBaseView.class),
                versionList.stream()
                        .map(app -> modelMapper.map(app, ApplicationView.class))
                        .toList()
        );
    }

    @GetMapping(value = "/versions/{id}")
    @Transactional
    public Set<ApplicationVersion> getApplicationVersion(@PathVariable Long id) {
        return this.getVersions(id);
    }

    @GetMapping(value = "/version/{id}")
    @Transactional
    public ApplicationView getApplication(@PathVariable Long id) {
        Application app = getApp(id);
        return modelMapper.map(app, ApplicationView.class);
    }

    /**
     * Use this method to add new ApplicationVersion and Application for existing ApplicationBaseEntity
     *
     * @param view      - application entity view
     * @param principal - security object (used to retrieve creator)
     */
    @PostMapping(value = "/version")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void addApplicationVersion(@RequestBody @Valid ApplicationView view, Principal principal) {

        this.applicationBaseOwnerCheck(view.getName(), principal);

        // validate
        // application base with given name must exist
        ApplicationBase base = applicationBaseService.findByName(view.getName());
        // specified version for this application base must not exist
        boolean hasVersion = base.getVersions()
                .stream()
                .anyMatch(v -> v.getVersion().equals(view.getVersion())
                        && !v.isDeleted());
        // application specified name and version must not exist
        if (hasVersion) {
            log.error("Cannot add application version, object already exists");
            throw new ObjectAlreadyExistsException("App version already exists");
        }

        // create application stub to avoid problems with circular dependencies
        // see application -> app config spec -> config file template -> application (id) :)
        Application temp = applicationService.create(new Application(view.getName(), view.getVersion()));
        Long appId = temp.getId();

        // create application entity & set properties
        Application application = modelMapper.map(view, Application.class);
        application.setId(appId);
        application.setState(ApplicationState.NEW);
        application.setCreationDate(LocalDateTime.now());
        applicationService.setMissingProperties(application, appId);
        ApplicationServiceImpl.clearIds(application);
        applicationService.checkAndUpdateConfigurationTemplate(application);
        applicationService.update(application);

        // create, add and persist new application version
        ApplicationVersion version = new ApplicationVersion(application.getVersion(), ApplicationState.NEW, appId);
        base.getVersions().add(version);
        applicationBaseService.update(base);

        notifyApplicationStateChange(application, new ApplicationStateChangeRequest(ApplicationStateDto.valueOf(application.getState().name()), "", false));
    }

    @PatchMapping(value = "/version")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void updateApplicationVersion(@RequestBody @Valid ApplicationView view, Principal principal) {

        this.applicationBaseOwnerCheck(view.getName(), principal);

        // check if id exists
        if (view.getId() == null) {
            log.error("ID is not present in Application update");
            throw new ProcessingException("Cannot update application without id");
        }

        // application with specified name and version must exist
        Optional<Application> optId = applicationService.findApplication(view.getId());
        Optional<Application> optNameVersion = applicationService.findApplication(view.getName(), view.getVersion());

        if (optId.isEmpty() || optNameVersion.isEmpty()) {
            log.error("Requested application does not exist");
            throw new MissingElementException("Application does not exist");
        }

        if (!optId.get().equals(optNameVersion.get())) {
            log.error("Retrieved different applications using id and name&version, update aborted");
            throw new ProcessingException("You cannot change application name, version and id");
        }

        // application base with given name must exist
        ApplicationBase base = applicationBaseService.findByName(view.getName());

        // you cannot really change version label
        Optional<ApplicationVersion> version = base.getVersions().stream()
                .filter(v -> v.getVersion().equals(view.getVersion()) && v.getAppVersionId().equals(view.getId()))
                .findFirst();

        if (version.isEmpty()) {
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
     * @param id                 application id (not an ApplicationBase or ApplicationVersion id)
     * @param stateChangeRequest request object
     */
    @PatchMapping(value = "/state/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public void changeApplicationState(@PathVariable long id, @RequestBody ApplicationStateChangeRequest stateChangeRequest, Principal principal) {
        Application app = getApp(id);
        if (stateChangeRequest.getState().equals(ApplicationStateDto.DELETED)) {
            long numberOfRunningInstances = applicationInstanceService.findAllByApplication(app).stream()
                    .filter(ai -> !applicationInstanceService.isInAnyState(ai.getId(), List.of(AppInstanceState.DONE, AppInstanceState.FAILURE, AppInstanceState.REMOVED)))
                    .count();
            if (numberOfRunningInstances > 0) {
                throw new ProcessingException("Can't set state to DELETED. There is still " + numberOfRunningInstances + " running instances of this version.");
            }
        }
        applicationService.changeApplicationState(app, ApplicationState.valueOf(stateChangeRequest.getState().name()));
        applicationBaseService.updateApplicationVersionState(app.getName(), app.getVersion(), ApplicationState.valueOf(stateChangeRequest.getState().name()));
        this.notifyApplicationStateChange(app, stateChangeRequest);
    }

    /**
     * Deletes application entity, labels an application version as deleted
     *
     * @param id application id (not an ApplicationBase or ApplicationVersion id)
     */
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteApplication(@PathVariable long id, Principal principal) {
        Application app = getApp(id);
        this.applicationBaseOwnerCheck(app.getName(), principal);
        applicationService.delete(id);
        applicationBaseService.updateApplicationVersionState(app.getName(), app.getVersion(), ApplicationState.DELETED);
    }

    /*
     * Utilities
     */
    private void notifyApplicationStateChange(Application app, ApplicationStateChangeRequest stateChangeRequest) {
        String appBaseName = app.getName().contains("_DELETED_")
                ? app.getName().substring(0, app.getName().indexOf("_DELETED_"))
                : app.getName();

        Map<String, Object> attributes = Map.of(
                "app_name", appBaseName,
                "app_version", app.getVersion(),
                "reason", stateChangeRequest.getReason() == null ? "" : stateChangeRequest.getReason(),
                "message", stateChangeRequest.getNotificationText() == null ? "" : stateChangeRequest.getNotificationText());
        if (!stateChangeRequest.getState().equals(ApplicationStateDto.ACTIVE)) {
            ApplicationBase applicationBase = applicationBaseService.findByName(appBaseName);
            UserDto owner = modelMapper.map(userService.findByUsername(applicationBase.getOwner()).orElseThrow(() -> new IllegalArgumentException("Owner not found")), UserDto.class);
            MailAttributes mailAttributes = MailAttributes.builder()
                    .mailType(ApplicationState.valueOf(stateChangeRequest.getState().name()).getMailType())
                    .addresses(Collections.singletonList(owner))
                    .otherAttributes(attributes)
                    .build();
            this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
        }
        if (stateChangeRequest.getState().equals(ApplicationStateDto.ACTIVE) && stateChangeRequest.shouldSendNotification()) {
            List<UserDto> users = userService.findAll()
                    .stream()
                    .filter(User::isEnabled)
                    .map(user -> modelMapper.map(user, UserDto.class))
                    .toList();
            MailAttributes mailAttributes = MailAttributes.builder()
                    .mailType(MailType.NEW_ACTIVE_APP)
                    .addresses(users)
                    .otherAttributes(attributes)
                    .build();
            this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
        }
    }

    private void applicationBaseOwnerCheck(ApplicationBase applicationBase, Principal principal) {
        boolean isSystemAdmin = this.getUser(principal.getName()).getRoles().stream()
                .anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN));
        boolean isOwner = applicationBase.getOwner().equals(principal.getName());
        if (!isOwner && !isSystemAdmin) {
            throw new PortalException("The user is not application owner");
        }
    }

    private void applicationBaseOwnerCheck(String applicationBaseName, Principal principal) {
        ApplicationBase applicationBase = this.applicationBaseService.findByName(applicationBaseName);
        this.applicationBaseOwnerCheck(applicationBase, principal);
    }

    private void applicationBaseOwnerCheck(Long id, Principal principal) {
        ApplicationBase applicationBase = this.applicationBaseService.getBaseApp(id);
        this.applicationBaseOwnerCheck(applicationBase, principal);
    }

}

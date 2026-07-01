package net.geant.nmaas.portal.api.apps;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.applications.AppRateDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseInfoDto;
import net.geant.nmaas.api.dto.applications.ApplicationDto;
import net.geant.nmaas.api.dto.applications.ApplicationExportDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.repositories.RatingRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/apps/base")
@Slf4j
@Tag(name = "Application Bases", description = "Operations related to application base entities")
public class ApplicationBaseController extends AppBaseController {

    private final RatingRepository ratingRepository;
    private final ApplicationSubscriptionService applicationSubscriptionService;

    @Autowired
    public ApplicationBaseController(ModelMapper modelMapper,
                                     ApplicationService applicationService,
                                     ApplicationBaseService applicationBaseService,
                                     UserService userService,
                                     RatingRepository ratingRepository,
                                     ApplicationSubscriptionService applicationSubscriptionService) {
        super(modelMapper, userService, applicationService, applicationBaseService);
        this.ratingRepository = ratingRepository;
        this.applicationSubscriptionService = applicationSubscriptionService;
    }

    @GetMapping
    @Transactional
    public List<ApplicationBaseInfoDto> getAllActiveApplicationBase() {
        return applicationBaseService.findAllActiveAppsSmall();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public List<ApplicationBaseDto> getAllApplicationBaseBasedByRole(Principal principal) {
        boolean isSystemAdmin = this.getUser(principal.getName()).getRoles().stream()
                .anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN));

        return applicationBaseService.findAll().stream()
                .filter(app -> isSystemAdmin || app.getOwner().equals(principal.getName()))
                .map(app -> modelMapper.map(app, ApplicationBaseDto.class))
                .map(this::setAppRating)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional
    public ApplicationBaseDto getApplicationBase(@PathVariable Long id) {
        ApplicationBaseDto app = modelMapper.map(applicationBaseService.getBaseApp(id), ApplicationBaseDto.class);
        return this.setAppRating(app);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void updateApplicationBase(@RequestBody ApplicationBaseDto baseView, Principal principal) {
        this.applicationBaseOwnerCheck(baseView.getName(), principal);
        applicationBaseService.update(modelMapper.map(baseView, ApplicationBase.class));
    }

    @PatchMapping("/{id}/owner/{owner}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void updateApplicationBaseOwner(@PathVariable Long id, @PathVariable String owner, Principal principal) {
        log.info("Updating owner of application {} to {}", id, owner);
        this.applicationBaseOwnerCheck(id, principal);
        applicationBaseService.updateOwner(id, owner);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteApplicationBase(@PathVariable Long id, Principal principal) {
        ApplicationBase base = applicationBaseService.getBaseApp(id);
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

    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public ApplicationBaseDto getApplicationBase(@PathVariable String name) {
        ApplicationBaseDto app = modelMapper.map(applicationBaseService.findByName(name), ApplicationBaseDto.class);
        return this.setAppRating(app);
    }

    @GetMapping("/{id}/versions")
    @Transactional
    public ApplicationExportDto getDtoWithBaseAndAllVersions(@PathVariable Long id) {
        ApplicationBase base = applicationBaseService.getBaseApp(id);
        List<Application> versionList = applicationService.findAll().stream()
                .filter(app -> app.getName().equalsIgnoreCase(base.getName()))
                .toList();
        return new ApplicationExportDto(
                modelMapper.map(base, ApplicationBaseDto.class),
                versionList.stream()
                        .map(app -> modelMapper.map(app, ApplicationDto.class))
                        .toList()
        );
    }

    private ApplicationBaseDto setAppRating(ApplicationBaseDto baseView) {
        Integer[] rating = ratingRepository.getApplicationRating(baseView.getId());
        baseView.setRate(createAppRate(rating));
        return baseView;
    }

    private static AppRateDto createAppRate(Integer[] rating) {
        return new AppRateDto(
                Arrays.stream(rating).mapToInt(Integer::intValue).average().orElse(0.0),
                Arrays.stream(rating).collect(Collectors.groupingBy(s -> s, Collectors.counting()))
        );
    }
}

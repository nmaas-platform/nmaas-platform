package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.api.dto.applications.AppRateDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseView;
import net.geant.nmaas.api.dto.applications.ApplicationSubscriptionBase;
import net.geant.nmaas.api.dto.applications.ApplicationSubscriptionDto;
import net.geant.nmaas.portal.persistence.repositories.RatingRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
public class ApplicationSubscriptionController extends AppBaseController {

    private final ApplicationSubscriptionService appSubscriptions;
    private final RatingRepository ratingRepository;

    @Autowired
    public ApplicationSubscriptionController(ModelMapper modelMapper, ApplicationService applicationService, ApplicationBaseService appBaseService, UserService userService, ApplicationSubscriptionService appSubscriptions, RatingRepository ratingRepository) {
        super(modelMapper, userService, applicationService, appBaseService);
        this.appSubscriptions = appSubscriptions;
        this.ratingRepository = ratingRepository;
    }

    @PostMapping
    @PreAuthorize("hasPermission(#appSubscription.domainId, 'domain', 'OWNER')")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@RequestBody ApplicationSubscriptionBase appSubscription) {
        appSubscriptions.subscribe(appSubscription.getApplicationId(), appSubscription.getDomainId(), true);
    }

    @PostMapping("/request")
    @PreAuthorize("hasPermission(#appSubscription.domainId, 'domain', 'ANY')")
    @Transactional
    public void subscribeRequest(@RequestBody ApplicationSubscriptionBase appSubscription) {
        appSubscriptions.subscribe(appSubscription.getApplicationId(), appSubscription.getDomainId(), false);
    }

    @DeleteMapping("/apps/{appId}/domains/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void unsubscribe(@PathVariable Long domainId, @PathVariable Long appId) {
        appSubscriptions.unsubscribe(appId, domainId);
    }

    @GetMapping("/apps/{appId}/domains/{domainId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApplicationSubscriptionDto> getSubscription(@PathVariable Long domainId, @PathVariable Long appId) {
        Optional<ApplicationSubscriptionDto> appSub = appSubscriptions.getSubscription(appId, domainId).map(sub -> modelMapper.map(sub, ApplicationSubscriptionDto.class));
        return appSub.map(applicationSubscription -> new ResponseEntity<>(applicationSubscription, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ApplicationSubscriptionBase> getAllSubscriptions() {
        return appSubscriptions.getSubscriptions().stream().filter(appSub -> !appSub.isDeleted())
                .map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
    }

    @GetMapping("/domains/{domainId}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
    public List<ApplicationSubscriptionBase> getDomainSubscriptions(@PathVariable Long domainId) {
        return appSubscriptions.getSubscriptionsBy(domainId, null).stream()
                .map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
    }

    @GetMapping("/domains/{domainId}/apps")
    @Transactional(readOnly = true)
    @PreAuthorize("hasPermission(#domainId, 'domain', 'READ')")
    public List<ApplicationBaseView> getDomainSubscribedApplications(@PathVariable Long domainId) {
        return appSubscriptions.getSubscribedApplications(domainId).stream()
                .map(app -> modelMapper.map(app, ApplicationBaseView.class))
                .map(this::setAppRating)
                .toList();
    }

    @GetMapping("/apps")
    @Transactional(readOnly = true)
    public List<ApplicationBaseView> getSubscribedApplications() {
        return appSubscriptions.getSubscribedApplications().stream()
                .map(app -> modelMapper.map(app, ApplicationBaseView.class))
                .map(this::setAppRating)
                .toList();
    }

    @GetMapping("/apps/{appId}")
    @Transactional(readOnly = true)
    public List<ApplicationSubscriptionBase> getApplicationSubscriptions(@PathVariable Long appId) {
        return appSubscriptions.getSubscriptionsBy(null, appId).stream()
                .map(appSub -> modelMapper.map(appSub, ApplicationSubscriptionBase.class)).collect(Collectors.toList());
    }

    private ApplicationBaseView setAppRating(ApplicationBaseView baseView) {
        Integer[] rating = this.ratingRepository.getApplicationRating(baseView.getId());
        baseView.setRate(createAppRateView(rating));
        return baseView;
    }

    private static AppRateDto createAppRateView(Integer[] rating) {
        return new AppRateDto(
                Arrays.stream(rating).mapToInt(Integer::intValue).average().orElse(0.0),
                Arrays.stream(rating).collect(Collectors.groupingBy(s -> s, Collectors.counting()))
        );
    }

}

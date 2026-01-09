package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistence.entity.Domain;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubscriptionService {

    boolean isActive(ApplicationSubscription.Id id);

    boolean isActive(Long applicationId, Long domainId);

    boolean isActive(String appName, Domain domain);

    boolean existsSubscription(ApplicationSubscription.Id id);

    boolean existsSubscription(Long applicationId, Long domainId);

    boolean existsSubscription(ApplicationBase application, Domain domain);

    Optional<ApplicationSubscription> getSubscription(ApplicationSubscription.Id id);

    Optional<ApplicationSubscription> getSubscription(Long applicationId, Long domainId);

    Optional<ApplicationSubscription> getSubscription(ApplicationBase application, Domain domain);

    List<ApplicationSubscription> getSubscriptions();

    List<ApplicationSubscription> getSubscriptionsBy(Long domainId, Long applicationId);

    List<ApplicationSubscription> getSubscriptionsBy(Domain domain, ApplicationBase application);

    ApplicationSubscription subscribe(ApplicationSubscription appSub);

    ApplicationSubscription subscribe(Long applicationId, Long domainId, boolean active);

    void unsubscribe(ApplicationSubscription appSub);

    void unsubscribe(Long applicationId, Long domainId);

    void unsubscribeAll(ApplicationBase applicationBase);

    List<ApplicationBase> getSubscribedApplications();

    List<ApplicationBase> getSubscribedApplications(Long domainId);

}

package net.geant.nmaas.portal.service;

import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueEntry;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentReviewEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.portal.api.bulk.BulkAppDetails;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentView;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.bulk.BulkQueueDetails;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.InputStreamResource;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BulkApplicationService {

    BulkDeploymentViewS handleBulkDeployment(String applicationName, List<CsvApplication> appInstanceSpecs, UserViewMinimal creator, Integer limit);

    ApplicationEvent handleDeploymentStatusUpdate(AppAutoDeploymentStatusUpdateEvent event);

    void handleDeploymentReview(AppAutoDeploymentReviewEvent event);

    List<BulkAppDetails> getAppsBulkDetails(BulkDeploymentView view);

    InputStreamResource getInputStreamAppBulkDetails(List<BulkAppDetails> list);

    void deleteAppInstancesFromBulk(BulkDeployment bulk);

    BulkDeployment updateState(Long bulkId);

    void updateBulkApplicationStatus();

    void updateEntryStateById(Long entryId);

    boolean validateDomainsList(Set<String> domainsName);

    void setBulkEntryToProcessing(Long bulkEntryId);

    BulkQueueDetails getQueueDetails(Long bulkId);

    Optional<BulkDeploymentEntry> getBulkEntry(Long bulkEntryId);

    void setBulkToCancel(BulkDeploymentQueueEntry queueEntry );

}
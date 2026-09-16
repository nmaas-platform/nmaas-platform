package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.bulks.BulkAppDetailsDto;
import net.geant.nmaas.api.dto.bulks.BulkDeploymentBaseDto;
import net.geant.nmaas.api.dto.bulks.BulkDeploymentDto;
import net.geant.nmaas.api.dto.bulks.BulkQueueDto;
import net.geant.nmaas.api.dto.users.UserInfoDto;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueEntry;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentReviewEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.persistence.entity.BulkDeployment;
import net.geant.nmaas.portal.persistence.entity.BulkDeploymentEntry;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.InputStreamResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BulkApplicationService {

    BulkDeploymentBaseDto handleBulkDeployment(String applicationName, List<CsvApplication> appInstanceSpecs, UserInfoDto creator, Integer limit);

    ApplicationEvent handleDeploymentStatusUpdate(AppAutoDeploymentStatusUpdateEvent event);

    void handleDeploymentReview(AppAutoDeploymentReviewEvent event);

    List<BulkAppDetailsDto> getAppsBulkDetails(BulkDeploymentDto view);

    InputStreamResource getInputStreamAppBulkDetails(List<BulkAppDetailsDto> list);

    void deleteAppInstancesFromBulk(BulkDeployment bulk);

    BulkDeployment updateState(Long bulkId);

    void updateBulkApplicationStatus();

    void updateEntryStateById(Long entryId);

    boolean validateDomainsList(Set<String> domainsName);

    void setBulkEntryToProcessing(Long bulkEntryId);

    BulkQueueDto getQueueDetails(Long bulkId);

    Optional<BulkDeploymentEntry> getBulkEntry(Long bulkEntryId);

    void cancelBulkEntry(BulkDeploymentQueueEntry queueEntry);

    void updateMainState(BulkDeployment bulkDeployment);

}

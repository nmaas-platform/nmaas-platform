package net.geant.nmaas.portal.service;

import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentReviewEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.portal.api.bulk.BulkAppDetails;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentView;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public interface BulkApplicationService {

    BulkDeploymentViewS handleBulkDeployment(String applicationName, List<CsvApplication> appInstanceSpecs, UserViewMinimal creator);

    ApplicationEvent handleDeploymentStatusUpdate(AppAutoDeploymentStatusUpdateEvent event);

    void handleDeploymentReview(AppAutoDeploymentReviewEvent event);

    List<BulkAppDetails> getAppsBulkDetails(BulkDeploymentView view);

}

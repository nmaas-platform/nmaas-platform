package net.geant.nmaas.webhooks.jobs;

import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import net.geant.nmaas.api.dto.webhooks.AppDeploymentWebhookDto;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.RestClient;

public abstract class AppWebhookJob extends WebhookJob {

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final KubernetesRepositoryManager serviceInfoRepositoryManager;

    public AppWebhookJob(RestClient restClient, WebhookEventService webhookEventService, ModelMapper modelMapper, AppDeploymentRepositoryManager appDeploymentRepositoryManager, KubernetesRepositoryManager serviceInfoRepositoryManager, WebhookHistoryService webhookHistoryService, AutoWebhookTemplateService templateService) {
        super(restClient, webhookEventService, modelMapper, webhookHistoryService, templateService);
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.serviceInfoRepositoryManager = serviceInfoRepositoryManager;
    }

    protected AppDeploymentWebhookDto getWebhookDto(String deploymentId) {
        Identifier identifier = Identifier.newInstance(deploymentId);
        AppDeployment appDeployment = appDeploymentRepositoryManager.load(identifier);
        AppDeploymentWebhookDto.AppDeploymentView appDeploymentView = modelMapper.map(appDeployment, AppDeploymentWebhookDto.AppDeploymentView.class);
        AppDeploymentWebhookDto webhookDto = new AppDeploymentWebhookDto(appDeploymentView, WebhookEventTypeDto.APPLICATION_DEPLOYMENT);
        webhookDto.setAppData(serviceInfoRepositoryManager.loadService(identifier).getAdditionalParameters());
        return webhookDto;
    }

}

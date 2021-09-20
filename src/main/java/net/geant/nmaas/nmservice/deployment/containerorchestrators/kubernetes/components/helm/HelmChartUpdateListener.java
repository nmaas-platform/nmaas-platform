package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.events.ApplicationListUpdatedEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

import static net.geant.nmaas.portal.events.ApplicationListUpdatedEvent.ApplicationAction.ADDED;
import static net.geant.nmaas.portal.events.ApplicationListUpdatedEvent.ApplicationAction.UPDATED;

@Component
@AllArgsConstructor
public class HelmChartUpdateListener {

    @Autowired
    private final HelmCommandExecutor helmCommandExecutor;

    @EventListener
    @Loggable(LogLevel.INFO)
    public ApplicationEvent trigger(ApplicationListUpdatedEvent event) {
        // add Helm repository from KubernetesTemplate (it will be overwritten if already exists)
        if (Arrays.asList(ADDED, UPDATED).contains(event.getAction())) {
            String repoName = event.getDeploymentSpec().getKubernetesTemplate().getHelmChartRepository().getName();
            String repoUrl = event.getDeploymentSpec().getKubernetesTemplate().getHelmChartRepository().getUrl();
            if (StringUtils.hasText(repoName) && StringUtils.hasText(repoUrl)) {
                helmCommandExecutor.executeHelmRepoAddCommand(repoName, repoUrl);
            }
        }
        return null;
    }

}

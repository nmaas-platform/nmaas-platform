package net.geant.nmaas.kubernetes.shell;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiClientFactory;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesConnectorHelper {

    private static final String SHELL_ACCESS_ENABLED_POD_LABEL = "shell-access-enabled";

    private final ApplicationInstanceService applicationInstanceService;
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    private final KubernetesApiClientFactory configFactory;

    public boolean checkAppInstanceSupportsSshAccess(Long appInstanceId) {
        log.debug("Checking if application instance with id {} supports SSH access", appInstanceId);
        boolean sshAccessAllowed = applicationInstanceService.find(appInstanceId)
                .orElseThrow(() -> new RuntimeException("App Instance not found"))
                .getApplication()
                .getAppDeploymentSpec()
                .isAllowSshAccess();
        log.debug("... returning {}", sshAccessAllowed);
        return sshAccessAllowed;
    }

    public Map<String, String> getPodNamesForAppInstance(Long appInstanceId) {
        log.debug("Retrieving names of pods for application instance with id {}", appInstanceId);
        if (!checkAppInstanceSupportsSshAccess(appInstanceId)) {
            throw new ProcessingException(String.format("Can't retrieve pod names for application instance %s", appInstanceId));
        }
        return getPodNamesForAppInstance(
                applicationInstanceService.find(appInstanceId).orElseThrow(
                        () -> new RuntimeException("App Instance not found"))
        );
    }

    private Map<String, String> getPodNamesForAppInstance(AppInstance appInstance) {
        final String namespace = appInstance.getDomain().getCodename();
        // TODO add support for remote clusters
        final String prefix = appDeploymentRepositoryManager.load(appInstance.getInternalId())
                .getDescriptiveDeploymentId().getValue();

        KubernetesClient client = configFactory.getClient();
        PodList podList = client.pods().inNamespace(namespace).list();

        return podList.getItems().stream()
                .filter(pod -> Boolean.parseBoolean(pod.getMetadata().getLabels().getOrDefault(SHELL_ACCESS_ENABLED_POD_LABEL, "false")))
                .map(pod -> new SimpleEntry<>(
                        pod.getMetadata().getName(),
                        pod.getMetadata().getLabels().getOrDefault("app", pod.getMetadata().getName()))
                )
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

}

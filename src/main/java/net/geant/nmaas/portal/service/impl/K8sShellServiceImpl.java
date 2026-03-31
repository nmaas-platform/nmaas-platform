package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.K8sPodInfoDto;
import net.geant.nmaas.api.dto.K8sShellCommandRequest;
import net.geant.nmaas.kubernetes.shell.KubernetesConnectorHelper;
import net.geant.nmaas.kubernetes.shell.PodSessionsStorage;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.service.K8sShellService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class K8sShellServiceImpl implements K8sShellService {

    private final PodSessionsStorage podSessionsStorage;
    private final KubernetesConnectorHelper connectorHelper;

    @Override
    public List<K8sPodInfoDto> getPodNames(Long appInstanceId) {
        if (!connectorHelper.checkAppInstanceSupportsSshAccess(appInstanceId)) {
            throw new ProcessingException(String.format("Can't retrieve pod names for application instance %s", appInstanceId));
        }
        return connectorHelper.getPodNamesForAppInstance(appInstanceId).entrySet().stream()
                .map(entry -> new K8sPodInfoDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public String createNewShellSession(Long appInstanceId, String podName) {
        return podSessionsStorage.createSession(appInstanceId, podName);
    }

    @Override
    public SseEmitter getEmitterForShellSession(String sessionId) {
        return podSessionsStorage.getObserver(sessionId).getEmitter();
    }

    @Override
    public void executeShellCommand(String sessionId, K8sShellCommandRequest commandRequest) {
        podSessionsStorage.executeCommand(sessionId, commandRequest);
    }

    @Override
    public void teardownShellSession(String sessionId) {
        podSessionsStorage.completeSession(sessionId);
    }

}

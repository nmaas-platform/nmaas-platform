package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.kubernetes.shell.PodSessionsStorage;
import net.geant.nmaas.kubernetes.KubernetesConnectorHelper;
import net.geant.nmaas.portal.api.domain.K8sPodInfo;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.service.K8sLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class K8sLogServiceImpl implements K8sLogService {

    private final PodSessionsStorage podSessionsStorage;
    private final KubernetesConnectorHelper connectorHelper;

    @Override
    public List<K8sPodInfo> getPodNames(Long appInstanceId) {
        if (!connectorHelper.checkAppInstanceSupportsLogAccess(appInstanceId)) {
            throw new ProcessingException(String.format("Can't retrieve pod names for application instance %s", appInstanceId));
        }
        return connectorHelper.getPodNamesForAppInstance(appInstanceId).entrySet().stream()
                .map(entry -> new K8sPodInfo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public String createNewLogSession(Long appInstanceId, String podName) {
        return null;
    }

    @Override
    public List<String> readLogs(String sessionId) {
        return null;
    }

    @Override
    public void teardownLogSession(String sessionId) {

    }
}

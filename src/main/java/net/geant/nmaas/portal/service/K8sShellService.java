package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.K8sPodInfo;
import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface K8sShellService {

    List<K8sPodInfo> getPodNames(Long appInstanceId);

    String createNewShellSession(Long appInstanceId, String podName);

    SseEmitter getEmitterForShellSession(String sessionId);

    void executeShellCommand(String sessionId, K8sShellCommandRequest commandRequest);

    void teardownShellSession(String sessionId);
}

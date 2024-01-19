package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.K8sPodInfo;

import java.util.List;

public interface K8sLogService {

    List<K8sPodInfo> getPodNames(Long appInstanceId);

    String createNewLogSession(Long appInstanceId, String podName);

    List<String> readLogs(String sessionId);

    void teardownLogSession(String sessionId);
}

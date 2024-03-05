package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.logs.PodInfo;
import net.geant.nmaas.portal.api.logs.PodLogs;

import java.util.List;

public interface ApplicationLogsService {

    boolean isLogAccessEnabled(Long appInstanceId);

    List<PodInfo> getPodNames(Long appInstanceId);

    PodLogs getPodLogs(Long appInstanceId, String podName, String containerName);
}

package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.orchestration.AppComponentLogs;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.portal.api.logs.PodInfo;
import net.geant.nmaas.portal.api.logs.PodLogs;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationLogsServiceImpl implements ApplicationLogsService {

    private final ApplicationInstanceService applicationInstanceService;
    private final AppDeploymentMonitor appDeploymentMonitor;

    @Override
    public boolean isLogAccessEnabled(Long appInstanceId) {
        return applicationInstanceService.find(appInstanceId)
                .orElseThrow(IllegalArgumentException::new)
                .getApplication()
                .getAppDeploymentSpec()
                .isAllowLogAccess();
    }

    @Override
    public List<PodInfo> getPodNames(Long appInstanceId) {
        AppInstance appInstance = applicationInstanceService.find(appInstanceId)
                .orElseThrow(IllegalArgumentException::new);
        return appDeploymentMonitor.appComponents(appInstance.getInternalId()).stream()
                .map(c -> new PodInfo(c.getName(), c.getDisplayName(), c.getSubComponents()))
                .collect(Collectors.toList());
    }

    @Override
    public PodLogs getPodLogs(Long appInstanceId, String podName, String containerName) {
        AppInstance appInstance = applicationInstanceService.find(appInstanceId)
                .orElseThrow(IllegalArgumentException::new);
        AppComponentLogs appComponentLogs = appDeploymentMonitor.appComponentLogs(appInstance.getInternalId(), podName, containerName);
        return new PodLogs(appComponentLogs.getName(), processLogs(appComponentLogs.getLines()));
    }

    private List<String> processLogs(List<String> lines) {
        return lines.stream().map(line -> {
            String ansiEscapeCodePattern = "\u001B\\[[;\\d]*m";
            Pattern pattern = Pattern.compile(ansiEscapeCodePattern);
            Matcher matcher = pattern.matcher(line);
            return matcher.replaceAll("");
        }).collect(Collectors.toList());
    }
}

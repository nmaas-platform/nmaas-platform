package net.geant.nmaas.portal.api.logs;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/apps/logs")
@AllArgsConstructor
@Transactional
@Slf4j
@Tag(name = "Application Instance Logs", description = "Access to running application logs")
public class AppLogsController {

    private final ApplicationLogsService service;

    /**
     * Retrieves pod names for an AppInstance
     *
     * @param appInstanceId identifier of AppInstance to retrieve pod names
     * @return names of pods and corresponding service names (to be displayed to the user)
     */
    @GetMapping(value = "/{appInstanceId}/pods")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'ANY')")
    public List<PodInfo> getPodNames(@PathVariable Long appInstanceId) {
        if (service.isLogAccessEnabled(appInstanceId)) {
            return service.getPodNames(appInstanceId);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Retrieves logs for a given pod of AppInstance
     *
     * @param appInstanceId identifier of AppInstance to retrieve pod names
     * @param podName       name of a pod
     * @param containerName name of a specific container inside the pod
     * @param limit         number of lines
     */
    @GetMapping(value = "/{appInstanceId}/pods/{podName}/container/{containerName}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'ANY')")
    public PodLogs getPodLogs(
            @PathVariable Long appInstanceId,
            @PathVariable String podName,
            @PathVariable String containerName,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        if (service.isLogAccessEnabled(appInstanceId)) {
            return service.getPodLogs(appInstanceId, podName, containerName, Objects.isNull(limit) ? 0 : limit);
        } else {
            throw new IllegalStateException();
        }
    }

}

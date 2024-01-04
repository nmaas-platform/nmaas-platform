package net.geant.nmaas.portal.api.logs;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/apps/logs")
@AllArgsConstructor
@Transactional
@Log4j2
public class AppLogsController {

    private final ApplicationLogsService service;

    /**
     * Retrieves pod names for an AppInstance
     * @param appInstanceId identifier of AppInstance to retrieve pod names
     * @return names of pods and corresponding service names (to be displayed to the user)
     */
    @GetMapping(value = "/{appInstanceId}/pods")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public List<PodInfo> getPodNames(@PathVariable Long appInstanceId) {
        if (service.isLogAccessEnabled(appInstanceId)) {
            return service.getPodNames(appInstanceId);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Retrieves logs for given pod of AppInstance
     * @param appInstanceId identifier of AppInstance to retrieve pod names
     * @param podName name of a pod
     */
    @GetMapping(value = "/{appInstanceId}/pods/{podName}")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public PodLogs getPodLogs(@PathVariable Long appInstanceId, @PathVariable String podName) {
        if (service.isLogAccessEnabled(appInstanceId)) {
            return service.getPodLogs(appInstanceId, podName);
        } else {
            throw new IllegalStateException();
        }
    }

}

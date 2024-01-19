package net.geant.nmaas.portal.api.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.domain.K8sPodInfo;
import net.geant.nmaas.portal.service.K8sLogService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling pod Log access
 */
@RestController
@RequestMapping("/api/pods/log")
@RequiredArgsConstructor
@Log4j2
public class PodLogController {

    private final K8sLogService k8sLogService;

    /**
     * Initializes connection if not exists
     * @param principal - principal
     * @param appInstanceId - target application instance identifier
     * @param podName - name of target connection kubernetes pod
     * @return session identifier
     */
    @PostMapping(value = "/{appInstanceId}/init/{podName}", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public String init(Principal principal, @PathVariable Long appInstanceId, @PathVariable String podName) {
        return k8sLogService.createNewLogSession(appInstanceId, podName);
    }

    @GetMapping(value = "/{id}")
    public List<String> readLogs(@PathVariable String logSessionId) {
        return k8sLogService.readLogs(logSessionId);
    }

    /**
     * This method is responsible for completing session, closing and removing connection
     * @param logSessionId - session identifier
     */
    @DeleteMapping(value = "/{id}")
    public void complete(@PathVariable String logSessionId) {
        k8sLogService.teardownLogSession(logSessionId);
    }

    /**
     * Retrieves pod names for given application instance
     * @param principal - principal
     * @param appInstanceId - identifier of application instance
     * @return names of pods and corresponding service names (to be displayed to the user)
     */
    @GetMapping(value = "/{appInstanceId}/podnames")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    public List<K8sPodInfo> getPodNames(Principal principal, @PathVariable Long appInstanceId) {
        log.debug("Retrieving list of pods for application instance {}", appInstanceId);
        return k8sLogService.getPodNames(appInstanceId);
    }

}

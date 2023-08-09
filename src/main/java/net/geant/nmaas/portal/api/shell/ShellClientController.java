package net.geant.nmaas.portal.api.shell;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.domain.K8sPodInfo;
import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;
import net.geant.nmaas.portal.service.K8sShellService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling SSH Shell over SSE
 * - create connection
 * - connect to SSE stream of results
 * - execute commands
 * - close connections
 */
@RestController
@RequestMapping("/api/shell")
@RequiredArgsConstructor
@Log4j2
public class ShellClientController {

    private final K8sShellService k8sShellService;

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
        return k8sShellService.createNewShellSession(appInstanceId, podName);
    }

    /**
     * Returns stream of events happening on the shell
     * @param principal - principal
     * @param sessionId - session identifier
     * @return SSE stream of events
     */
    @CrossOrigin
    @GetMapping(value = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getShell(Principal principal, @PathVariable String sessionId) {
        return k8sShellService.getEmitterForShellSession(sessionId);
    }

    /**
     * Sending commands to the shell
     * @param principal - principal
     * @param sessionId - session identifier
     * @param commandRequest - command or signal to be executed
     */
    @PostMapping(value = "/{id}/command")
    public void execute(Principal principal, @PathVariable String sessionId, @RequestBody K8sShellCommandRequest commandRequest) {
        k8sShellService.executeShellCommand(sessionId, commandRequest);
    }

    /**
     * This method is responsible for completing session, closing and removing connection
     * @param principal - principal
     * @param sessionId - session identifier
     */
    @DeleteMapping(value = "/{id}")
    public void complete(Principal principal, @PathVariable String sessionId) {
        k8sShellService.teardownShellSession(sessionId);
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
        return k8sShellService.getPodNames(appInstanceId);
    }

}

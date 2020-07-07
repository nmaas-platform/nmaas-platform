package net.geant.nmaas.portal.api.shell;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.shell.connectors.KubernetesConnectorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/api")
@Log4j2
public class ShellClientController {

    private final ShellSessionsStorage storage;

    private final KubernetesConnectorHelper connectorHelper;

    @Autowired
    public ShellClientController(ShellSessionsStorage storage,
                                 KubernetesConnectorHelper connectorHelper){
        this.storage = storage;
        this.connectorHelper = connectorHelper;
    }

    /**
     * initialize connection if not exists
     * FUTURE: possibly allows generating multiple connections to the same instance
     * @param principal
     * @param id - target app instance id
     * @param podName - name of target connection kubernetes pod
     * @return - session id
     */
    @PostMapping(value = "/shell/{id}/init/{podName}", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasPermission(#id, 'appInstance', 'OWNER')")
    public String init(Principal principal, @PathVariable Long id, @PathVariable String podName) {
        return this.storage.createSession(id, podName);
    }

    /**
     * Returns stream of events happening on the shell
     * @param principal
     * @param id - session id (returned by init)
     * @return SSE stream of events
     */
    @CrossOrigin
    @GetMapping(value = "/shell/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getShell(Principal principal, @PathVariable String id){
        return this.storage.getObserver(id).getEmitter();
    }

    /**
     * This is function is responsible for sending commands to the shell
     * @param principal
     * @param id session id (returned by init)
     * @param commandRequest command or signal to be executed
     */
    @PostMapping(value = "/shell/{id}/command")
    public void execute(Principal principal, @PathVariable String id, @RequestBody ShellCommandRequest commandRequest) {
        this.storage.executeCommand(id, commandRequest);
    }

    /**
     * This method is responsible for completing session, closing and removing connection
     * @param principal
     * @param id session id
     */
    @DeleteMapping(value = "/shell/{id}")
    public void complete(Principal principal, @PathVariable String id) {
        this.storage.completeSession(id);
    }

    /**
     * Retrieves pod names for an AppInstance
     * @param principal
     * @param id identifier of AppInstance to retrieve pod names
     * @return list of pod names
     */
    @GetMapping(value = "/shell/{id}/podnames")
    public List<String> getPodNames(Principal principal, @PathVariable Long id) {
        return this.connectorHelper.getPodNamesForAppInstance(id);
    }
}

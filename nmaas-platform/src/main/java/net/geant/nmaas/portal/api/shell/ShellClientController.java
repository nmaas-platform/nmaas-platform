package net.geant.nmaas.portal.api.shell;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

/**
 * POC
 * SSH Shell over SSE
 */
@RestController
@RequestMapping("/api")
@Log4j2
public class ShellClientController {

    private ShellSessionsStorage storage;

    @Autowired
    public ShellClientController(ShellSessionsStorage storage){
        this.storage = storage;
    }

    /**
     * initialize connection if not exists
     * FUTURE: possibly allows generating multiple connections to the same instance
     * @param principal
     * @param id - target app instance id
     * @return - session id
     */
    @PostMapping(value = "/shell/{id}/init", produces = MediaType.TEXT_PLAIN_VALUE)
    public String init(Principal principal, @PathVariable Long id){
        return this.storage.createSession(id);
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
}

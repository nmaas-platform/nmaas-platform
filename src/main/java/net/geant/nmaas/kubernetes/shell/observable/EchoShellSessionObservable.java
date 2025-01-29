package net.geant.nmaas.kubernetes.shell.observable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * PoC for command execution, does echo
 */
@Slf4j
public class EchoShellSessionObservable extends GenericShellSessionObservable implements Serializable {

    private final String sessionId;
    private final transient ObjectMapper objectMapper = new ObjectMapper();

    public EchoShellSessionObservable(String sessionId) {
        this.sessionId = sessionId;
    }

    public void executeCommand(K8sShellCommandRequest commandRequest) {
        Map<String, Object> map = new HashMap<>();
        map.put("session", this.sessionId);
        map.put("date", LocalDateTime.now());
        map.put("command", commandRequest);
        try {
            String result = objectMapper.writeValueAsString(map);
            this.setChanged();
            this.notifyObservers(result); // send update to observers
        } catch (JsonProcessingException e) {
            log.warn(String.format("Exception caught (%s)", e.getMessage()));
            log.warn(Arrays.toString(e.getStackTrace()));
            log.info("Failed to send command execution result");
        }
    }

    @Override
    public void executeCommandAsync(K8sShellCommandRequest commandRequest) {
        this.executeCommand(commandRequest);
    }

}

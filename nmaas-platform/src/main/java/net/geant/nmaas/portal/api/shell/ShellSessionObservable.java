package net.geant.nmaas.portal.api.shell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * this class is responsible for command execution logic,
 * currently does nothing but echo
 */
@Log4j2
public class ShellSessionObservable extends Observable {

    private final String sessionId;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // TODO some state
    public ShellSessionObservable(String sessionId) {
        this.sessionId = sessionId;
    }

    public void executeCommand(ShellCommandRequest commandRequest) {
        // TODO handle logic (produce result)
        Map<String, Object> map = new HashMap<>();
        map.put("session", this.sessionId);
        map.put("date", LocalDateTime.now());
        map.put("command", commandRequest);
        try{
            String result = objectMapper.writeValueAsString(map);
            this.setChanged();
            this.notifyObservers(result); // send update to observers
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.info("Failed to send command execution result");
        }

    }
}

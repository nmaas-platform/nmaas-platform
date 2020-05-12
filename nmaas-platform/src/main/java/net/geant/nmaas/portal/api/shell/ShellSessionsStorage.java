package net.geant.nmaas.portal.api.shell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * service layer component for handling connections
 */
@Component
public class ShellSessionsStorage {

    @Getter
    @AllArgsConstructor
    private class ObserverObservablePair implements Serializable {
        private final ShellSessionObserver observer;
        private final ShellSessionObservable observable;
    }

    // dummy storage
    private final Map<String, ObserverObservablePair> storage = new ConcurrentHashMap<>();

    /**
     * creates connection and session id
     * @param appInstanceId app instance identifier
     * @return shell session id
     */
    public String createSession(Long appInstanceId) {
        // TODO setup connection to given app instance id
        // Handle logic here
        String sessionId = UUID.randomUUID().toString();
        if(this.storage.containsKey(sessionId)) {
            // TODO handle
            return "error";
        }

        // create observer and observable and chain them
        ShellSessionObservable observable = new ShellSessionObservable(sessionId);
        ShellSessionObserver observer = new ShellSessionObserver();
        observable.addObserver(observer);

        storage.put(sessionId, new ObserverObservablePair(observer, observable));

        return sessionId;
    }

    /**
     * returns an observer with event emitter
     * @param sessionId shell session id
     * @return observer with event emitter
     */
    public ShellSessionObserver getObserver(String sessionId) {
        return this.storage.get(sessionId).getObserver();
    }

    /**
     * executes command
     * @param sessionId shell session id
     * @param commandRequest command to be executed
     */
    public void executeCommand(String sessionId, ShellCommandRequest commandRequest) {
        this.storage.get(sessionId).getObservable().executeCommand(commandRequest);
    }
}

package net.geant.nmaas.portal.api.shell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * service layer component for handling connections
 */
@Component
@AllArgsConstructor
public class ShellSessionsStorage {

    private final ApplicationInstanceService instanceService;

    @Getter
    @AllArgsConstructor
    private class ObserverObservablePair implements Serializable {
        private final ShellSessionObserver observer;
        private final ShellSessionObservable observable;

        private void complete() {
            observer.complete();
            observable.complete();
        }
    }

    // dummy storage
    private final Map<String, ObserverObservablePair> storage = new ConcurrentHashMap<>();

    /**
     * creates connection and session id
     * this method is synchronized, so assigned session id will not be assigned in a meantime
     * @param appInstanceId app instance identifier
     * @return shell session id
     */
    public synchronized String createSession(Long appInstanceId) {
        AppInstance instance = this.instanceService.find(appInstanceId)
                .orElseThrow(() -> new RuntimeException("This application instance does not exists"));
        // TODO check if you can connect to this app instance
        // TODO extract connection parameters

        String sessionId = UUID.randomUUID().toString();
        // new session id must be unique
        while (this.storage.containsKey(sessionId)) {
            sessionId = UUID.randomUUID().toString();
        }

        // create observer and observable and bind them
        // TODO pass connection params to observable
        ShellSessionObservable observable = new ShellSessionObservable(sessionId);
        ShellSessionObserver observer = new ShellSessionObserver();
        observable.addObserver(observer);

        storage.putIfAbsent(sessionId, new ObserverObservablePair(observer, observable));

        return sessionId;
    }

    /**
     * returns an observer with event emitter
     * @param sessionId shell session id
     * @return observer with event emitter
     */
    public ShellSessionObserver getObserver(String sessionId) {
        isSessionAvailable(sessionId);
        return this.storage.get(sessionId).getObserver();
    }

    /**
     * executes command
     * @param sessionId shell session id
     * @param commandRequest command to be executed
     */
    public void executeCommand(String sessionId, ShellCommandRequest commandRequest) {
        isSessionAvailable(sessionId);
        this.storage.get(sessionId).getObservable().executeCommand(commandRequest);
    }

    /**
     * complete the session;
     * complete connection,
     * remove connection from storage
     * @param sessionId
     */
    public void completeSession(String sessionId) {
        isSessionAvailable(sessionId);
        ObserverObservablePair element = storage.get(sessionId);
        element.complete();
        storage.remove(sessionId, element);
    }

    /**
     * checks if session with given id is available
     * @param sessionId
     */
    private void isSessionAvailable(String sessionId) {
        if(!this.storage.containsKey(sessionId)){
            throw new RuntimeException("Session with id: " + sessionId + " does not exist");
        }
    }
}

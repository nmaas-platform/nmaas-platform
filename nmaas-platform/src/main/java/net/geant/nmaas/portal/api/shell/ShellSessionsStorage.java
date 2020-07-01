package net.geant.nmaas.portal.api.shell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.utils.ssh.SshSessionConnector;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer component for handling connections
 * ShellSessionObservable is in charge of creating ssh connection, by default SshConnectionShellSessionObservable is used
 * ShellSessionObserver observes given observable and is a sink for the command execution results
 * Default sink utilizes SSE to pass results to the client
 */
@Component
@AllArgsConstructor
public class ShellSessionsStorage {

    private final ApplicationInstanceService instanceService;

    /**
     * this class stores observer-observable pair
     * in future it can be extended to store multiple observers for single observable
     */
    @Getter
    @AllArgsConstructor
    private static class ObserverObservablePair implements Serializable {
        private final ShellSessionObserver observer;
        private final GenericShellSessionObservable observable;

        private void complete() {
            observer.complete();
            observable.complete();
        }
    }

    // dummy storage
    private final Map<String, ObserverObservablePair> storage = new ConcurrentHashMap<>();

    /**
     * creates connection and session id
     * this method is synchronized, so assigned session id will not be re-assigned in a meantime
     * @param appInstanceId app instance identifier
     * @return shell session id
     */
    public synchronized String createSession(Long appInstanceId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        AppInstance instance = this.instanceService.find(appInstanceId)
                .orElseThrow(() -> new RuntimeException("This application instance does not exists"));
        // check if you can connect to this app instance
        if(!instance.getApplication().getAppDeploymentSpec().isAllowSshAccess()) {
            throw new RuntimeException("SSH connection is not allowed");
        }

        String sessionId = UUID.randomUUID().toString();
        // new session id must be unique
        while (this.storage.containsKey(sessionId)) {
            sessionId = UUID.randomUUID().toString();
        }

        // create ssh connector
        // TODO extract connection parameters from app instance
        // TODO replace default connector with app instance specific connectors
        SshSessionConnector defaultConnector = SshConnectionShellSessionObservable.getDefaultConnector();

        // create observer and observable and bind them
        GenericShellSessionObservable observable = new SshConnectionShellSessionObservable(sessionId, defaultConnector);
        ShellSessionObserver observer = new ShellSessionObserver();
        observable.addObserver(observer);

        storage.putIfAbsent(sessionId, new ObserverObservablePair(observer, observable));

        return sessionId;
    }

    /**
     * returns an observer with event emitter for given emitter
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
        this.storage.get(sessionId).getObservable().executeCommandAsync(commandRequest);
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

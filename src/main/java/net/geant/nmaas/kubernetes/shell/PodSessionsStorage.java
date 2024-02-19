package net.geant.nmaas.kubernetes.shell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.kubernetes.AsyncConnector;
import net.geant.nmaas.kubernetes.AsyncConnectorFactory;
import net.geant.nmaas.kubernetes.shell.observable.GenericShellSessionObservable;
import net.geant.nmaas.kubernetes.shell.observable.SshConnectionShellSessionObservable;
import net.geant.nmaas.kubernetes.shell.observer.ShellSessionObserver;
import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.springframework.stereotype.Component;

import java.io.Serializable;
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
@RequiredArgsConstructor
public class PodSessionsStorage {

    // dummy storage
    private final Map<String, ObserverObservablePair> storage = new ConcurrentHashMap<>();

    private final ApplicationInstanceService instanceService;
    private final AsyncConnectorFactory connectorFactory;

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

    /**
     * Creates pod connection and associates it with custom generated session identifier
     * this method is synchronized, so assigned session id will not be re-assigned in a meantime
     * @param appInstanceId app instance identifier
     * @return shell session id
     */
    public synchronized String createSession(Long appInstanceId, String podName) {
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

        AsyncConnector connector = null;
        if (podName == null ) {
            connector = connectorFactory.preparePodShellConnection(instance);
        } else {
            connector = connectorFactory.preparePodShellConnection(instance, podName);
        }

        // create observer and observable and bind them
        GenericShellSessionObservable observable = new SshConnectionShellSessionObservable(sessionId, connector);
        ShellSessionObserver observer = new ShellSessionObserver();
        observable.addObserver(observer);

        storage.putIfAbsent(sessionId, new ObserverObservablePair(observer, observable));

        return sessionId;
    }

    public synchronized String createSession(Long appInstanceId) {
        return this.createSession(appInstanceId, null);
    }

    /**
     * Returns an observer with event emitter for given session
     * @param sessionId sSession identifier
     * @return observer with event emitter
     */
    public ShellSessionObserver getObserver(String sessionId) {
        isSessionAvailable(sessionId);
        return this.storage.get(sessionId).getObserver();
    }

    /**
     * Executes command in given session
     * @param sessionId Session identifier
     * @param commandRequest Command to be executed
     */
    public void executeCommand(String sessionId, K8sShellCommandRequest commandRequest) {
        isSessionAvailable(sessionId);
        this.storage.get(sessionId).getObservable().executeCommandAsync(commandRequest);
    }

    /**
     * Completes given session and removes it from storage
     * @param sessionId Session identifier
     */
    public void completeSession(String sessionId) {
        isSessionAvailable(sessionId);
        ObserverObservablePair element = storage.get(sessionId);
        element.complete();
        storage.remove(sessionId, element);
    }

    /**
     * Checks if session with given id is available
     * @param sessionId Session identifier
     */
    private void isSessionAvailable(String sessionId) {
        if (!this.storage.containsKey(sessionId)) {
            throw new RuntimeException("Session with id: " + sessionId + " does not exist");
        }
    }
}

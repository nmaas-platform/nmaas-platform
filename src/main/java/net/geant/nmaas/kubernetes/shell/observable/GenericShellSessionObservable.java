package net.geant.nmaas.kubernetes.shell.observable;

import net.geant.nmaas.api.dto.K8sShellCommandRequest;
import net.geant.nmaas.kubernetes.shell.observer.ShellSessionObserver;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for shell session event sources.
 * Replaces the deprecated java.util.Observable with a simple,
 * thread-safe observer registry ({@link CopyOnWriteArrayList}).
 */
public abstract class GenericShellSessionObservable implements Serializable {

    private final transient List<ShellSessionObserver> observers = new CopyOnWriteArrayList<>();

    public abstract void executeCommand(K8sShellCommandRequest commandRequest);

    public abstract void executeCommandAsync(K8sShellCommandRequest commandRequest);

    public void addObserver(ShellSessionObserver observer) {
        observers.add(observer);
    }

    public void deleteObservers() {
        observers.clear();
    }

    protected void notifyObservers(Object message) {
        observers.forEach(observer -> observer.update(message));
    }

    public void complete() {
        this.deleteObservers();
    }
}

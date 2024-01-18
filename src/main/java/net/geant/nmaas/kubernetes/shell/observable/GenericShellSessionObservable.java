package net.geant.nmaas.kubernetes.shell.observable;

import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;

import java.io.Serializable;
import java.util.Observable;

public abstract class GenericShellSessionObservable extends Observable implements Serializable {

    public abstract void executeCommand(K8sShellCommandRequest commandRequest);

    public abstract void executeCommandAsync(K8sShellCommandRequest commandRequest);

    public void complete() {
        this.deleteObservers();
    }
}

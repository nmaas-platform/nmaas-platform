package net.geant.nmaas.portal.api.shell.observable;

import net.geant.nmaas.portal.api.shell.ShellCommandRequest;

import java.util.Observable;

public abstract class GenericShellSessionObservable extends Observable {

    public abstract void executeCommand(ShellCommandRequest commandRequest);

    public abstract void executeCommandAsync(ShellCommandRequest commandRequest);

    public void complete() {
        this.deleteObservers();
    }
}

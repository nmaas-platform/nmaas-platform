package net.geant.nmaas.portal.api.shell;

import java.util.Observable;

public abstract class GenericShellSessionObservable extends Observable {

    public abstract void executeCommand(ShellCommandRequest commandRequest);

    public void complete() {
        this.deleteObservers();
    }
}

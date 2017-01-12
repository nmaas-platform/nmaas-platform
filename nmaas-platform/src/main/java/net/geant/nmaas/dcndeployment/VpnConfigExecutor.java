package net.geant.nmaas.dcndeployment;

public class VpnConfigExecutor {

    private String command;

    public void execute() {
        new Thread((Runnable) () -> {

            ShellCommandRunner.executeCommand(command);

        }).start();
    }

    public void command(String command) {
        this.command = command;
    }
}

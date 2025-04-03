package net.geant.nmaas.utils.bash;

public interface CommandExecutor {

    void execute(Command command) throws CommandExecutionException;

    String executeWithOutput(Command command) throws CommandExecutionException;

}
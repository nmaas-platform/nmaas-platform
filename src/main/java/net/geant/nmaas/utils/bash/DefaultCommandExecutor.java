package net.geant.nmaas.utils.bash;

import org.springframework.stereotype.Component;

@Component
public class DefaultCommandExecutor implements CommandExecutor {

    @Override
    public void execute(Command command) {

    }

    @Override
    public String executeWithOutput(Command command) throws CommandExecutionException {
        return "";
    }

}

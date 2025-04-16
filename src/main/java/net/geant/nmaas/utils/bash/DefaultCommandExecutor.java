package net.geant.nmaas.utils.bash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DefaultCommandExecutor implements CommandExecutor {

    @Override
    public void execute(Command command) throws CommandExecutionException {
        try {
            new ProcessBuilder(command.asString().split(" ")).start();
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    @Override
    public String executeWithOutput(Command command) throws CommandExecutionException {
        try {
            Process process = new ProcessBuilder(command.asString().split(" ")).start();
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Output: {}", output);
            return output;
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

}

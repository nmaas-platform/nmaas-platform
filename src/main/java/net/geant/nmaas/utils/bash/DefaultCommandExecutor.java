package net.geant.nmaas.utils.bash;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

@Slf4j
@Component
public class DefaultCommandExecutor implements CommandExecutor {

    @Override
    public void execute(Command command) throws CommandExecutionException {
        executeInternal(command);
    }

    @Override
    public String executeWithOutput(Command command) throws CommandExecutionException {
        return executeInternal(command);
    }

    private String executeInternal(Command command) throws CommandExecutionException {
        try {
            log.info("Executing: {}", command);
            Process process = new ProcessBuilder(command.asString().split(" ")).start();
            final String errorOutput = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            if (Strings.isNotEmpty(errorOutput)) {
                throw new CommandExecutionException("Error received during command execution (details: " + errorOutput + ")");
            }
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            validateOutput(output, command.isOutputCorrect());
            return output;
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    private static void validateOutput(String output, Predicate<String> isOutputCorrect) {
        if (isOutputCorrect.negate().test(output)) {
            throw new CommandExecutionException("Identified problem with command execution based on output (details: " + output + ")");
        }
    }

}
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
            log.info("Executing: {}", command.asString());
            Process process = new ProcessBuilder(new String[]{"sh", "-c", command.asString()}).start();
            final String errorOutput = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            if (Strings.isNotEmpty(errorOutput)) {
                // warnings should not be considered as errors
                log.warn("Some command execution information present in the error output");
                log.debug(errorOutput);
                log.warn("Verifying error output");
                validateOutput(errorOutput, command.isOutputCorrect());
            }
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.trace("Verifying standard output");
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

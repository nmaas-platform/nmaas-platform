package net.geant.nmaas.utils.ssh;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.commands.HelmListCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SingleCommandExecutorTest {

    private HelmListCommand command;

    private SingleCommandExecutor executor;

    @BeforeEach
    public void setup() {
        command = HelmListCommand.command("v2", "namespace", false);
        executor = SingleCommandExecutor.getExecutor("", "");
    }

    @Test
    public void shouldProperlyValidateCommandExecutionOutput() throws CommandExecutionException {
        executor.validateOutput("No error string at the beginning ...", command.isOutputCorrect());
    }

    @Test
    public void shouldProperlyValidateCommandExecutionOutputAndThrowExceptionOnError() throws CommandExecutionException {
        assertThrows(CommandExecutionException.class, () -> {
            executor.validateOutput("Error ...", command.isOutputCorrect());
        });
    }

}

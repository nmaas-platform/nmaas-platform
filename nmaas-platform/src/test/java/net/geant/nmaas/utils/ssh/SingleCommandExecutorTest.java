package net.geant.nmaas.utils.ssh;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmListCommand;
import org.junit.Before;
import org.junit.Test;

public class SingleCommandExecutorTest {

    private HelmListCommand command;

    private SingleCommandExecutor executor;

    @Before
    public void setup() {
        command = HelmListCommand.command(false);
        executor = SingleCommandExecutor.getExecutor("", "");
    }

    @Test
    public void shouldProperlyValidateCommandExecutionOutput() throws CommandExecutionException {
        executor.validateOutput("No error string at the beginning ...", command.isOutputCorrect());
    }

    @Test(expected = CommandExecutionException.class)
    public void shouldProperlyValidateCommandExecutionOutputAndThrowExceptionOnError() throws CommandExecutionException {
        executor.validateOutput("Error ...", command.isOutputCorrect());
    }

}

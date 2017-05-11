package net.geant.nmaas.utils.ssh;

import net.geant.nmaas.nmservice.configuration.ConfigDownloadCommand;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SingleCommandExecutorTest {

    private ConfigDownloadCommand command;

    private SingleCommandExecutor executor;

    @Before
    public void setup() {
        command = ConfigDownloadCommand.command("", "", "", "", "");
        executor = SingleCommandExecutor.getExecutor("", "");
    }

    @Test
    public void shouldProperlyValidateCommandExecutionOutput() throws CommandExecutionException {
        executor.validateOutput("aslksjakld connected. klsjdlkasn ... 200", command.isOutputCorrect());
    }

    @Test(expected = CommandExecutionException.class)
    public void shouldProperlyValidateCommandExecutionOutputAndThrowExceptionOnWrongHttpStatus() throws CommandExecutionException {
        executor.validateOutput("aslksjakld connected. klsjdlkasn ... 401", command.isOutputCorrect());
    }

    @Test(expected = CommandExecutionException.class)
    public void shouldProperlyInterpretCommandExecutionOutputAndThrowExceptionOnMissingConnectedStatement() throws CommandExecutionException {
        executor.validateOutput("aslksjakld failure", command.isOutputCorrect());
    }

}

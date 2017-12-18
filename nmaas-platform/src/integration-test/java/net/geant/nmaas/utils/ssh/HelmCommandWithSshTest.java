package net.geant.nmaas.utils.ssh;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.HelmStatusCommand;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class HelmCommandWithSshTest {

    @Ignore
    @Test
    public void shouldExecuteCommandAndReturnOutput() throws SshConnectionException, CommandExecutionException {
        String output = SingleCommandExecutor
                .getExecutor("10.134.241.6", "nmaas")
                .executeSingleCommandAndReturnOutput(HelmStatusCommand.command("c21584cd-666c-42de-9df7-d72b7bae5aae"));
        assertThat(output, containsString("STATUS: DEPLOYED"));
    }

}

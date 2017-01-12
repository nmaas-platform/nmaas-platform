package net.geant.nmaas.dcndeployment;

import org.junit.Ignore;
import org.junit.Test;

public class ShellCommandRunnerTest {

    @Ignore
    @Test
    public void shouldExecutePingCommand() {
        System.out.println(ShellCommandRunner.executeCommand("ping 8.8.8.8"));
    }

}

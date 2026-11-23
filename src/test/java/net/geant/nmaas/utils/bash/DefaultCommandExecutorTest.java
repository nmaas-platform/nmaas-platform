package net.geant.nmaas.utils.bash;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultCommandExecutorTest {

    private final CommandExecutor executor = new DefaultCommandExecutor();

    @Disabled("Only for local test run manually")
    @Test
    void shouldRunExampleCommand() {
        String output = executor.executeWithOutput(new Command() {
            @Override
            public String asString() {
                return "ls";
            }

            @Override
            public Predicate<String> isOutputCorrect() {
                return null;
            }
        });
        assertNotNull(output);
    }

}

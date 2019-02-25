package net.geant.nmaas.orchestration.entities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AppDeploymentStateTest {

    @ParameterizedTest(name = "shouldReturnIsInFailedState for [{arguments}]")
    @EnumSource(
            value = AppDeploymentState.class,
            names = {"APPLICATION_CONFIGURATION_FAILED",
                    "APPLICATION_DEPLOYMENT_FAILED",
                    "APPLICATION_DEPLOYMENT_VERIFICATION_FAILED",
                    "APPLICATION_REMOVAL_FAILED",
                    "APPLICATION_RESTART_FAILED",
                    "DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED",
                    "REQUEST_VALIDATION_FAILED",
                    "INTERNAL_ERROR"})
    public void shouldReturnIsInFailedState(AppDeploymentState state) {
        assertThat(state.isInFailedState(), is(true));
    }

}

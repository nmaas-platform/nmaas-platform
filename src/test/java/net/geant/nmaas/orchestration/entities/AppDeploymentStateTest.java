package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_UPDATED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_UPDATE_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_PAUSED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_PAUSE_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_PAUSE_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVAL_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVAL_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_RESTARTED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_RESTART_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_RESTART_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_UPGRADED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_UPGRADE_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_UPGRADE_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.INTERNAL_ERROR;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.MANAGEMENT_VPN_CONFIGURED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.REQUESTED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.REQUEST_VALIDATED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.REQUEST_VALIDATION_FAILED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.values;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppDeploymentStateTest {

    @ParameterizedTest
    @EnumSource(
            value = AppDeploymentState.class,
            names = {"APPLICATION_CONFIGURATION_FAILED",
                    "APPLICATION_CONFIGURATION_UPDATE_FAILED",
                    "APPLICATION_DEPLOYMENT_FAILED",
                    "APPLICATION_DEPLOYMENT_VERIFICATION_FAILED",
                    "APPLICATION_REMOVAL_FAILED",
                    "APPLICATION_RESTART_FAILED",
                    "APPLICATION_PAUSE_FAILED",
                    "APPLICATION_UPGRADE_FAILED",
                    "DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED",
                    "REQUEST_VALIDATION_FAILED",
                    "INTERNAL_ERROR"})
    void shouldReturnIsInFailedState(AppDeploymentState state) {
        assertThat(state.isInFailedState(), is(true));
    }

    @ParameterizedTest
    @EnumSource(
            value = AppDeploymentState.class,
            names = {"APPLICATION_DEPLOYMENT_VERIFIED",
                    "APPLICATION_RESTARTED",
                    "APPLICATION_UPGRADED"})
    void shouldReturnIsInRunningState(AppDeploymentState state) {
        assertThat(state.isInRunningState(), is(true));
    }

    private static Stream<AppDeploymentState> allFailingStates() {
        return Arrays.stream(values()).filter(AppDeploymentState::isInFailedState);
    }

    @ParameterizedTest
    @MethodSource("allFailingStates")
    void shouldTransitFromFailingState(AppDeploymentState state) {
        assertThat(state.nextState(ServiceDeploymentState.INIT), is(REQUESTED));
        assertThat(state.nextState(ServiceDeploymentState.REMOVAL_INITIATED), is(APPLICATION_REMOVAL_IN_PROGRESS));
        assertThat(state.nextState(ServiceDeploymentState.FAILED_APPLICATION_REMOVED), is(FAILED_APPLICATION_REMOVED));
        assertThat(state.nextState(ServiceDeploymentState.VERIFICATION_INITIATED), is(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS));
    }

    private static Stream<AppDeploymentState> allRunningStates() {
        return Arrays.stream(values()).filter(AppDeploymentState::isInRunningState);
    }

    @ParameterizedTest
    @MethodSource("allRunningStates")
    void shouldTransitFromRunningState(AppDeploymentState state) {
        assertThat(state.nextState(ServiceDeploymentState.REMOVAL_INITIATED), is(APPLICATION_REMOVAL_IN_PROGRESS));
        assertThat(state.nextState(ServiceDeploymentState.RESTART_INITIATED), is(APPLICATION_RESTART_IN_PROGRESS));
        assertThat(state.nextState(ServiceDeploymentState.CONFIGURATION_UPDATE_INITIATED), is(APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS));
        assertThat(state.nextState(ServiceDeploymentState.PAUSE_INITIATED), is(APPLICATION_PAUSE_IN_PROGRESS));
    }

    @ParameterizedTest
    @MethodSource("allRunningStates")
    void shouldThrowInvalidAppStateExceptionOnIncorrectTransition(AppDeploymentState state) {
        assertThrows(InvalidAppStateException.class, () -> {
            state.nextState(ServiceDeploymentState.INIT);
        });
    }

    private static Stream<AppDeploymentState> allStatesButEndStates() {
        return Arrays.stream(values()).filter(state -> !state.isInEndState());
    }

    @ParameterizedTest
    @MethodSource("allStatesButEndStates")
    void shouldSupportAppInstanceRemovalInAllStates(AppDeploymentState state) {
        assertThat(state.nextState(ServiceDeploymentState.REMOVAL_INITIATED), is(APPLICATION_REMOVAL_IN_PROGRESS));
    }

    private static Stream<Arguments> stateTransitionsAndExpectedOutput() {
        return Stream.of(
                Arguments.of(REQUESTED, ServiceDeploymentState.REQUEST_VERIFIED, REQUEST_VALIDATED),
                Arguments.of(REQUESTED, ServiceDeploymentState.REQUEST_VERIFICATION_FAILED, REQUEST_VALIDATION_FAILED),
                Arguments.of(REQUEST_VALIDATED, ServiceDeploymentState.ENVIRONMENT_PREPARATION_INITIATED, DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS),
                Arguments.of(REQUEST_VALIDATED, ServiceDeploymentState.ENVIRONMENT_PREPARED, DEPLOYMENT_ENVIRONMENT_PREPARED),
                Arguments.of(REQUEST_VALIDATED, ServiceDeploymentState.ENVIRONMENT_PREPARATION_FAILED, DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS, ServiceDeploymentState.ENVIRONMENT_PREPARED, DEPLOYMENT_ENVIRONMENT_PREPARED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS, ServiceDeploymentState.ENVIRONMENT_PREPARATION_FAILED, DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARED, ServiceDeploymentState.ENVIRONMENT_PREPARATION_INITIATED, DEPLOYMENT_ENVIRONMENT_PREPARED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARED, ServiceDeploymentState.CONFIGURED, APPLICATION_CONFIGURED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARED, ServiceDeploymentState.READY_FOR_DEPLOYMENT, MANAGEMENT_VPN_CONFIGURED),
                Arguments.of(MANAGEMENT_VPN_CONFIGURED, ServiceDeploymentState.CONFIGURATION_INITIATED, APPLICATION_CONFIGURATION_IN_PROGRESS),
                Arguments.of(MANAGEMENT_VPN_CONFIGURED, ServiceDeploymentState.CONFIGURED, APPLICATION_CONFIGURED),
                Arguments.of(MANAGEMENT_VPN_CONFIGURED, ServiceDeploymentState.CONFIGURATION_FAILED, APPLICATION_CONFIGURATION_FAILED),
                Arguments.of(APPLICATION_CONFIGURED, ServiceDeploymentState.DEPLOYMENT_INITIATED, APPLICATION_DEPLOYMENT_IN_PROGRESS),
                Arguments.of(APPLICATION_CONFIGURED, ServiceDeploymentState.DEPLOYMENT_FAILED, APPLICATION_DEPLOYMENT_FAILED),
                Arguments.of(APPLICATION_DEPLOYMENT_IN_PROGRESS, ServiceDeploymentState.DEPLOYED, APPLICATION_DEPLOYED),
                Arguments.of(APPLICATION_DEPLOYMENT_IN_PROGRESS, ServiceDeploymentState.DEPLOYMENT_FAILED, APPLICATION_DEPLOYMENT_FAILED),
                Arguments.of(APPLICATION_DEPLOYED, ServiceDeploymentState.VERIFICATION_INITIATED, APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS),
                Arguments.of(APPLICATION_DEPLOYED, ServiceDeploymentState.VERIFICATION_FAILED, APPLICATION_DEPLOYMENT_VERIFICATION_FAILED),
                Arguments.of(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS, ServiceDeploymentState.VERIFIED, APPLICATION_DEPLOYMENT_VERIFIED),
                Arguments.of(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS, ServiceDeploymentState.VERIFICATION_FAILED, APPLICATION_DEPLOYMENT_VERIFICATION_FAILED),
                Arguments.of(APPLICATION_RESTART_IN_PROGRESS, ServiceDeploymentState.RESTARTED, APPLICATION_RESTARTED),
                Arguments.of(APPLICATION_RESTART_IN_PROGRESS, ServiceDeploymentState.RESTART_FAILED, APPLICATION_RESTART_FAILED),
                Arguments.of(APPLICATION_PAUSE_IN_PROGRESS, ServiceDeploymentState.PAUSED, APPLICATION_PAUSED),
                Arguments.of(APPLICATION_PAUSE_IN_PROGRESS, ServiceDeploymentState.PAUSE_FAILED, APPLICATION_PAUSE_FAILED),
                Arguments.of(APPLICATION_REMOVAL_IN_PROGRESS, ServiceDeploymentState.REMOVED, APPLICATION_REMOVED),
                Arguments.of(APPLICATION_REMOVAL_IN_PROGRESS, ServiceDeploymentState.REMOVAL_FAILED, APPLICATION_REMOVAL_FAILED),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS, ServiceDeploymentState.CONFIGURATION_UPDATED, APPLICATION_CONFIGURATION_UPDATED),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS, ServiceDeploymentState.CONFIGURATION_UPDATE_FAILED, APPLICATION_CONFIGURATION_UPDATE_FAILED),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATED, ServiceDeploymentState.VERIFICATION_INITIATED, APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATED, ServiceDeploymentState.VERIFICATION_FAILED, APPLICATION_DEPLOYMENT_VERIFICATION_FAILED),
                Arguments.of(APPLICATION_REMOVED, ServiceDeploymentState.CONFIGURATION_REMOVAL_INITIATED, APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS),
                Arguments.of(APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS, ServiceDeploymentState.CONFIGURATION_REMOVED, APPLICATION_CONFIGURATION_REMOVED),
                Arguments.of(APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS, ServiceDeploymentState.CONFIGURATION_REMOVAL_FAILED, APPLICATION_CONFIGURATION_REMOVAL_FAILED),
                Arguments.of(APPLICATION_CONFIGURATION_REMOVED, ServiceDeploymentState.FAILED_APPLICATION_REMOVED, FAILED_APPLICATION_REMOVED),
                Arguments.of(APPLICATION_UPGRADE_IN_PROGRESS, ServiceDeploymentState.UPGRADED, APPLICATION_UPGRADED),
                Arguments.of(APPLICATION_UPGRADE_IN_PROGRESS, ServiceDeploymentState.UPGRADE_FAILED, APPLICATION_UPGRADE_FAILED)
        );
    }

    @ParameterizedTest
    @MethodSource("stateTransitionsAndExpectedOutput")
    void shouldTransitThroughStates(AppDeploymentState fromState, ServiceDeploymentState onState, AppDeploymentState toState) {
        assertThat(fromState.nextState(onState), is(toState));
    }

    private static Stream<Arguments> deploymentAndExpectedLifecycleStates() {
        return Stream.of(
                Arguments.of(REQUESTED, AppLifecycleState.REQUESTED),
                Arguments.of(REQUEST_VALIDATED, AppLifecycleState.REQUEST_VALIDATED),
                Arguments.of(REQUEST_VALIDATION_FAILED, AppLifecycleState.REQUEST_VALIDATION_FAILED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS, AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARED, AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED),
                Arguments.of(DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED, AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED),
                Arguments.of(MANAGEMENT_VPN_CONFIGURED, AppLifecycleState.MANAGEMENT_VPN_CONFIGURED),
                Arguments.of(APPLICATION_CONFIGURATION_IN_PROGRESS, AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS),
                Arguments.of(APPLICATION_CONFIGURED, AppLifecycleState.APPLICATION_CONFIGURED),
                Arguments.of(APPLICATION_CONFIGURATION_FAILED, AppLifecycleState.APPLICATION_CONFIGURATION_FAILED),
                Arguments.of(APPLICATION_DEPLOYMENT_IN_PROGRESS, AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS),
                Arguments.of(APPLICATION_DEPLOYED, AppLifecycleState.APPLICATION_DEPLOYED),
                Arguments.of(APPLICATION_DEPLOYMENT_FAILED, AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED),
                Arguments.of(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS, AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS),
                Arguments.of(APPLICATION_DEPLOYMENT_VERIFIED, AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED),
                Arguments.of(APPLICATION_DEPLOYMENT_VERIFICATION_FAILED, AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_FAILED),
                Arguments.of(APPLICATION_RESTART_IN_PROGRESS, AppLifecycleState.APPLICATION_RESTART_IN_PROGRESS),
                Arguments.of(APPLICATION_RESTARTED, AppLifecycleState.APPLICATION_RESTARTED),
                Arguments.of(APPLICATION_RESTART_FAILED, AppLifecycleState.APPLICATION_RESTART_FAILED),
                Arguments.of(APPLICATION_PAUSE_IN_PROGRESS, AppLifecycleState.APPLICATION_PAUSE_IN_PROGRESS),
                Arguments.of(APPLICATION_PAUSED, AppLifecycleState.APPLICATION_PAUSED),
                Arguments.of(APPLICATION_PAUSE_FAILED, AppLifecycleState.APPLICATION_PAUSE_FAILED),
                Arguments.of(APPLICATION_REMOVAL_IN_PROGRESS, AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS),
                Arguments.of(APPLICATION_REMOVED, AppLifecycleState.APPLICATION_REMOVED),
                Arguments.of(APPLICATION_REMOVAL_FAILED, AppLifecycleState.APPLICATION_REMOVAL_FAILED),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS, AppLifecycleState.APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATED, AppLifecycleState.APPLICATION_CONFIGURATION_UPDATED),
                Arguments.of(APPLICATION_CONFIGURATION_UPDATE_FAILED, AppLifecycleState.APPLICATION_CONFIGURATION_UPDATE_FAILED),
                Arguments.of(APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS, AppLifecycleState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS),
                Arguments.of(APPLICATION_CONFIGURATION_REMOVED, AppLifecycleState.APPLICATION_CONFIGURATION_REMOVED),
                Arguments.of(APPLICATION_CONFIGURATION_REMOVAL_FAILED, AppLifecycleState.APPLICATION_CONFIGURATION_REMOVAL_FAILED),
                Arguments.of(APPLICATION_UPGRADE_IN_PROGRESS, AppLifecycleState.APPLICATION_UPGRADE_IN_PROGRESS),
                Arguments.of(APPLICATION_UPGRADED, AppLifecycleState.APPLICATION_UPGRADED),
                Arguments.of(APPLICATION_UPGRADE_FAILED, AppLifecycleState.APPLICATION_UPGRADE_FAILED),
                Arguments.of(INTERNAL_ERROR, AppLifecycleState.INTERNAL_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("deploymentAndExpectedLifecycleStates")
    void shouldReturnCorrectLifecycleState(AppDeploymentState state, AppLifecycleState lifecycleState) {
        assertThat(state.lifecycleState(), is(lifecycleState));
    }

}

package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.orchestration.AppLifecycleState.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppDeploymentOrchestratorTask {

    private final static Logger log = LogManager.getLogger(AppDeploymentOrchestratorTask.class);

    private static final int STATE_CHANGE_WAIT_INTERVAL_IN_MILIS = 500;

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    private AppDeploymentMonitor appDeploymentMonitor;

    private AppDeploymentStateChangeListener appDeploymentStateChangeListener;

    private AppDeploymentOrchestratorTaskHelper helper;

    @Autowired
    public AppDeploymentOrchestratorTask(NmServiceDeploymentProvider serviceDeployment,
                                         DcnDeploymentProvider dcnDeployment,
                                         AppDeploymentMonitor appDeploymentMonitor,
                                         AppDeploymentStateChangeListener appDeploymentStateChangeListener,
                                         AppDeploymentOrchestratorTaskHelper helper) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.appDeploymentStateChangeListener = appDeploymentStateChangeListener;
        this.helper = helper;
    }

    @Loggable(LogLevel.INFO)
    public void deploy(Identifier deploymentId, Identifier clientId, Identifier applicationId) {
        try {
            helper.verifyIfAllIdentifiersAreSet(deploymentId, clientId, applicationId);
            NmServiceInfo serviceInfo = serviceDeployment.verifyRequest(deploymentId, helper.constructNmServiceSpec(clientId, applicationId));
            if (serviceInfo == null)
                return;
            dcnDeployment.verifyRequest(deploymentId, helper.constructDcnSpec(clientId, applicationId, serviceInfo));
            waitForRequestValidatedState(deploymentId);
            serviceDeployment.prepareDeploymentEnvironment(deploymentId);
            dcnDeployment.prepareDeploymentEnvironment(deploymentId);
            waitForEnvironmentPreparedState(deploymentId);
            dcnDeployment.deployDcn(deploymentId);
            waitForVpnConfiguredState(deploymentId);
            waitForAppConfiguredState(deploymentId);
            serviceDeployment.deployNmService(deploymentId);
            waitForAppDeployedState(deploymentId);
            serviceDeployment.verifyNmService(deploymentId);
        } catch (InvalidAppStateException
                | InvalidDeploymentIdException
                | net.geant.nmaas.nmservice.InvalidDeploymentIdException
                | InvalidApplicationIdException
                | InterruptedException e) {
            log.error("Exception during application deployment -> " + e.getMessage());
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        } catch (Exception e) {
            log.error("Exception during application deployment -> " + e.getMessage());
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        }
    }

    private void waitForRequestValidatedState(Identifier deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(REQUEST_VALIDATED, deploymentId)) {
            if (stateIs(REQUEST_VALIDATION_FAILED, deploymentId))
                throw new InvalidAppStateException("Waited for " + REQUEST_VALIDATED + " but got " + REQUEST_VALIDATION_FAILED);
            throwExceptionIfStateChangedToInternalError(deploymentId);
            waitBeforeNextStateCheck();
        }
    }

    private void waitForEnvironmentPreparedState(Identifier deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(DEPLOYMENT_ENVIRONMENT_PREPARED, deploymentId)) {
            if (stateIs(DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED, deploymentId))
                throw new InvalidAppStateException("Waited for " + DEPLOYMENT_ENVIRONMENT_PREPARED + " but got " + DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED);
            throwExceptionIfStateChangedToInternalError(deploymentId);
            waitBeforeNextStateCheck();
        }
    }

    private void waitForVpnConfiguredState(Identifier deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(MANAGEMENT_VPN_CONFIGURED, deploymentId)) {
            if (stateIs(MANAGEMENT_VPN_CONFIGURATION_FAILED, deploymentId))
                throw new InvalidAppStateException("Waited for " + MANAGEMENT_VPN_CONFIGURED + " but got " + MANAGEMENT_VPN_CONFIGURATION_FAILED);
            throwExceptionIfStateChangedToInternalError(deploymentId);
            waitBeforeNextStateCheck();
        }
    }

    private void waitForAppConfiguredState(Identifier deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(APPLICATION_CONFIGURED, deploymentId)) {
            if (stateIs(APPLICATION_CONFIGURATION_FAILED, deploymentId))
                throw new InvalidAppStateException("Waited for " + APPLICATION_CONFIGURED + " but got " + APPLICATION_CONFIGURATION_FAILED);
            throwExceptionIfStateChangedToInternalError(deploymentId);
            waitBeforeNextStateCheck();
        }
    }

    private void waitForAppDeployedState(Identifier deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(APPLICATION_DEPLOYED, deploymentId)) {
            if (stateIs(APPLICATION_DEPLOYMENT_FAILED, deploymentId))
                throw new InvalidAppStateException("Waited for " + APPLICATION_DEPLOYED + " but got " + APPLICATION_DEPLOYMENT_FAILED);
            throwExceptionIfStateChangedToInternalError(deploymentId);
            waitBeforeNextStateCheck();
        }
    }

    private void throwExceptionIfStateChangedToInternalError(Identifier deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException {
        if (stateIs(INTERNAL_ERROR, deploymentId))
            throw new InvalidAppStateException("Waited for valid state but got " + INTERNAL_ERROR);
    }

    private boolean stateDifferentThen(AppLifecycleState state, Identifier deploymentId) throws InvalidDeploymentIdException {
        return !state.equals(appDeploymentMonitor.state(deploymentId));
    }

    private boolean stateIs(AppLifecycleState state, Identifier deploymentId) throws InvalidDeploymentIdException {
        return state.equals(appDeploymentMonitor.state(deploymentId));
    }

    private void waitBeforeNextStateCheck() throws InterruptedException {
        Thread.sleep(STATE_CHANGE_WAIT_INTERVAL_IN_MILIS);
    }

}

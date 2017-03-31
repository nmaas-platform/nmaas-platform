package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnSpec;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.orchestration.AppLifecycleState.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(value = "prototype")
public class AppDeploymentOrchestratorTask implements Runnable {

    private final static Logger log = LogManager.getLogger(AppDeploymentOrchestratorTask.class);

    private static final int STATE_CHANGE_WAIT_INTERVAL_IN_MILIS = 500;

    private NmServiceDeploymentProvider serviceDeployment;

    private DcnDeploymentProvider dcnDeployment;

    private AppDeploymentMonitor appDeploymentMonitor;

    private AppDeploymentStateChangeListener appDeploymentStateChangeListener;

    private ApplicationRepository applications;

    @Autowired
    public AppDeploymentOrchestratorTask(NmServiceDeploymentProvider serviceDeployment,
                                         DcnDeploymentProvider dcnDeployment,
                                         AppDeploymentMonitor appDeploymentMonitor,
                                         AppDeploymentStateChangeListener appDeploymentStateChangeListener,
                                         ApplicationRepository applications) {
        this.serviceDeployment = serviceDeployment;
        this.dcnDeployment = dcnDeployment;
        this.appDeploymentMonitor = appDeploymentMonitor;
        this.appDeploymentStateChangeListener = appDeploymentStateChangeListener;
        this.applications = applications;
    }

    private Identifier deploymentId;

    private Identifier clientId;

    private Identifier applicationId;

    public void populateIdentifiers(Identifier deploymentId, Identifier clientId, Identifier applicationId) {
        this.deploymentId = deploymentId;
        this.clientId = clientId;
        this.applicationId = applicationId;
    }

    @Override
    public void run() {
        deploy();
    }

    private void deploy() {
        try {
            verifyIfAllIdentifiersAreSet();
            NmServiceInfo serviceInfo = serviceDeployment.verifyRequest(deploymentId, constructNmServiceSpec(clientId, applicationId));
            dcnDeployment.verifyRequest(deploymentId, constructDcnSpec(clientId, applicationId, serviceInfo));
            waitForRequestValidatedState();
            serviceDeployment.prepareDeploymentEnvironment(deploymentId);
            dcnDeployment.prepareDeploymentEnvironment(deploymentId);
            waitForEnvironmentPreparedState();
            dcnDeployment.deployDcn(deploymentId);
            waitForVpnConfiguredState();
            waitForAppConfiguredState();
            serviceDeployment.deployNmService(deploymentId);
            waitForAppDeployedState();
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

    private void waitForRequestValidatedState() throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(REQUEST_VALIDATED)) {
            if (stateIs(REQUEST_VALIDATION_FAILED))
                throw new InvalidAppStateException("Waited for " + REQUEST_VALIDATED + " but got " + REQUEST_VALIDATION_FAILED);
            throwExceptionIfStateChangedToInternalError();
            waitBeforeNextStateCheck();
        }
    }

    private void waitForEnvironmentPreparedState() throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(DEPLOYMENT_ENVIRONMENT_PREPARED)) {
            if (stateIs(DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED))
                throw new InvalidAppStateException("Waited for " + DEPLOYMENT_ENVIRONMENT_PREPARED + " but got " + DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED);
            throwExceptionIfStateChangedToInternalError();
            waitBeforeNextStateCheck();
        }
    }

    private void waitForVpnConfiguredState() throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(MANAGEMENT_VPN_CONFIGURED)) {
            if (stateIs(MANAGEMENT_VPN_CONFIGURATION_FAILED))
                throw new InvalidAppStateException("Waited for " + MANAGEMENT_VPN_CONFIGURED + " but got " + MANAGEMENT_VPN_CONFIGURATION_FAILED);
            throwExceptionIfStateChangedToInternalError();
            waitBeforeNextStateCheck();
        }
    }

    private void waitForAppConfiguredState() throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(APPLICATION_CONFIGURED)) {
            if (stateIs(APPLICATION_CONFIGURATION_FAILED))
                throw new InvalidAppStateException("Waited for " + APPLICATION_CONFIGURED + " but got " + APPLICATION_CONFIGURATION_FAILED);
            throwExceptionIfStateChangedToInternalError();
            waitBeforeNextStateCheck();
        }
    }

    private void waitForAppDeployedState() throws InvalidDeploymentIdException, InvalidAppStateException, InterruptedException {
        while (stateDifferentThen(APPLICATION_DEPLOYED)) {
            if (stateIs(APPLICATION_DEPLOYMENT_FAILED))
                throw new InvalidAppStateException("Waited for " + APPLICATION_DEPLOYED + " but got " + APPLICATION_DEPLOYMENT_FAILED);
            throwExceptionIfStateChangedToInternalError();
            waitBeforeNextStateCheck();
        }
    }

    private void throwExceptionIfStateChangedToInternalError() throws InvalidDeploymentIdException, InvalidAppStateException {
        if (stateIs(INTERNAL_ERROR))
            throw new InvalidAppStateException("Waited for valid state but got " + INTERNAL_ERROR);
    }

    private boolean stateDifferentThen(AppLifecycleState state) throws InvalidDeploymentIdException {
        return !state.equals(appDeploymentMonitor.state(deploymentId));
    }

    private boolean stateIs(AppLifecycleState state) throws InvalidDeploymentIdException {
        return state.equals(appDeploymentMonitor.state(deploymentId));
    }

    private void waitBeforeNextStateCheck() throws InterruptedException {
        Thread.sleep(STATE_CHANGE_WAIT_INTERVAL_IN_MILIS);
    }

    private void verifyIfAllIdentifiersAreSet() throws Exception {
        if (deploymentId == null || clientId == null || applicationId == null)
            throw new Exception("Input parameters verification failed (" + deploymentId + " and " + clientId + " and " + applicationId + ")");
    }

    NmServiceSpec constructNmServiceSpec(Identifier clientId, Identifier applicationId)
            throws InvalidApplicationIdException {
        final Application application = applications.findOne(Long.valueOf(applicationId.getValue()));
        if (application == null)
            throw new InvalidApplicationIdException("Application with id " + applicationId + " does not exist in repository");
        return new DockerContainerSpec(
                buildServiceName(application),
                application.getDockerContainerTemplate(),
                Long.valueOf(clientId.getValue()));
    }

    String buildServiceName(Application application) {
        return application.getName() + "-" + application.getId();
    }

    private DcnSpec constructDcnSpec(Identifier clientId, Identifier applicationId, NmServiceInfo serviceInfo) {
        DcnSpec dcn = new DcnSpec(buildDcnName(applicationId, clientId));
        if (serviceInfo != null && serviceInfo.getNetwork() != null)
            dcn.setNmServiceDeploymentNetworkDetails(serviceInfo.getNetwork());
        else
            log.warn("Failed to set NM service deployment network details in DCN spec");
        return dcn;
    }

    private String buildDcnName(Identifier applicationId, Identifier clientId) {
        return clientId + "-" + applicationId;
    }

}

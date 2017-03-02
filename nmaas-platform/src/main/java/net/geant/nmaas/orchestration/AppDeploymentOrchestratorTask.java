package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnSpec;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineContainerTemplate;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.orchestration.AppLifecycleState.*;


/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope("prototype")
public class AppDeploymentOrchestratorTask implements Runnable {

    private static final int STATE_CHANGE_WAIT_INTERVAL_IN_MILIS = 500;

    @Autowired
    private NmServiceDeploymentProvider serviceDeployment;

    @Autowired
    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;

    @Autowired
    private AppDeploymentStateChangeListener appDeploymentStateChangeListener;

    @Autowired
    private NmServiceTemplateRepository applicationTemplates;

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
            serviceDeployment.verifyRequest(deploymentId, constructNmServiceSpec(clientId, applicationId));
            dcnDeployment.verifyRequest(deploymentId, constructDcnSpec(clientId, applicationId));
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
        } catch (InterruptedException e) {
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        } catch (InvalidAppStateException e) {
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        } catch (InvalidDeploymentIdException
                | net.geant.nmaas.nmservice.InvalidDeploymentIdException e) {
            appDeploymentStateChangeListener.notifyGenericError(deploymentId);
        } catch (InvalidApplicationIdException e) {
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

    private void verifyIfAllIdentifiersAreSet() {
        if (deploymentId == null || clientId == null || applicationId == null)
            throw new NullPointerException();
    }

    private NmServiceSpec constructNmServiceSpec(Identifier clientId, Identifier applicationId) throws InvalidApplicationIdException {
        final DockerEngineContainerTemplate template = (DockerEngineContainerTemplate) applicationTemplates.loadTemplateByApplicationId(applicationId);
        if (template == null)
            throw new InvalidApplicationIdException("Nm Service template for application id " + applicationId + " does not exist");
        final String serviceName = buildServiceName(applicationId, template);
        DockerContainerSpec dockerContainerSpec = new DockerContainerSpec(serviceName, template);
        // client details should be read from database
        dockerContainerSpec.setClientDetails("client-" + clientId, "organization-" + clientId);
        return dockerContainerSpec;
    }

    private String buildServiceName(Identifier applicationId, DockerEngineContainerTemplate template) {
        return template.getName() + "-" + applicationId;
    }

    private DcnSpec constructDcnSpec(Identifier clientId, Identifier applicationId) {
        return new DcnSpec(buildDcnName(applicationId, clientId));
    }

    private String buildDcnName(Identifier applicationId, Identifier clientId) {
        return clientId + "-" + applicationId;
    }

}

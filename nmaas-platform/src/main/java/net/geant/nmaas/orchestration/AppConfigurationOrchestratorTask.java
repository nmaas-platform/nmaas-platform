package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope("prototype")
public class AppConfigurationOrchestratorTask implements Runnable {

    @Autowired
    private NmServiceConfigurationProvider serviceConfiguration;

    private Identifier deploymentId;

    private AppConfiguration configuration;

    public void populateProperties(Identifier deploymentId, AppConfiguration configuration) {
        this.deploymentId = deploymentId;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        configure();
    }

    private void configure() {
        verifyIfAllPropertiesAreSet();
        try {
            serviceConfiguration.configureNmService(deploymentId, configuration);
        } catch (net.geant.nmaas.nmservice.InvalidDeploymentIdException e) {
            e.printStackTrace();
        }
    }

    private void verifyIfAllPropertiesAreSet() {
        if (deploymentId == null || configuration == null)
            throw new NullPointerException();
    }

}

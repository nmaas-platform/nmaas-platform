package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentMode;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DcnDeploymentTask {

    private final static Logger log = LogManager.getLogger(DcnDeploymentTask.class);

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public DcnDeploymentTask(DcnDeploymentProvider dcnDeployment) {
        this.dcnDeployment = dcnDeployment;
    }

    @Value("${dcn.deployment.mode}")
    private String mode;

    @EventListener
    public void deployDcn(DcnDeployActionEvent event) throws InvalidDeploymentIdException {
        final Identifier clientId = event.getClientId();
        try {
            if (DcnDeploymentMode.AUTO.toString().equals(mode))
                dcnDeployment.deployDcn(clientId);
        } catch (CouldNotDeployDcnException e) {
            log.warn("DCN deployment failed for client " + clientId.value() + " -> " + e.getMessage());
        }
    }
}

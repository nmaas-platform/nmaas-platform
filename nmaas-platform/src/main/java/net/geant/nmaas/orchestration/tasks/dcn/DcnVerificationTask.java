package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentMode;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
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
public class DcnVerificationTask {

    private final static Logger log = LogManager.getLogger(DcnVerificationTask.class);

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public DcnVerificationTask(DcnDeploymentProvider dcnDeployment) {
        this.dcnDeployment = dcnDeployment;
    }

    @Value("${dcn.deployment.mode}")
    private String mode;

    @EventListener
    public void verifyDcn(DcnVerifyActionEvent event) throws InvalidDeploymentIdException {
        final Identifier clientId = event.getClientId();
        try {
            if (DcnDeploymentMode.AUTO.value().equals(mode) || DcnDeploymentMode.MANUAL.value().equals(mode))
                dcnDeployment.verifyDcn(clientId);
        } catch (CouldNotVerifyDcnException e) {
            log.warn("DCN verification failed for client " + clientId.value() + " -> " + e.getMessage());
        }
    }
}

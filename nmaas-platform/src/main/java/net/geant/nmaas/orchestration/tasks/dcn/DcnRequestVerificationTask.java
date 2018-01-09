package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentMode;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DcnRequestVerificationTask extends BaseDcnTask {

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(DcnVerifyRequestActionEvent event) throws DcnRequestVerificationException {
        final Identifier clientId = event.getRelatedTo();
        if (DcnDeploymentMode.AUTO.value().equals(mode) || DcnDeploymentMode.MANUAL.value().equals(mode))
            dcnDeployment.verifyRequest(clientId, constructDcnSpec(clientId));
    }

    public DcnSpec constructDcnSpec(Identifier clientId) {
        return new DcnSpec(buildDcnName(clientId), clientId);
    }

    public String buildDcnName(Identifier clientId) {
        return clientId + "-" + System.nanoTime();
    }

}

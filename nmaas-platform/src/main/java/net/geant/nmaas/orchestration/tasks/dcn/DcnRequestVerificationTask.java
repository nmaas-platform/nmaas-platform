package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
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
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(DcnVerifyRequestActionEvent event) throws DcnRequestVerificationException {
        final String domain = event.getRelatedTo();
        dcnDeployment.verifyRequest(domain);
    }

}

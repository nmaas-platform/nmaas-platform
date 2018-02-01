package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DcnVerificationTask extends BaseDcnTask {

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DcnVerifyActionEvent event) throws CouldNotVerifyDcnException {
        final Identifier clientId = event.getRelatedTo();
        dcnDeployment.verifyDcn(clientId);
    }

}

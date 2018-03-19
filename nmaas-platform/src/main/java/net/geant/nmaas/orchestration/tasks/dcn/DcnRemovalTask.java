package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.orchestration.events.dcn.DcnRemoveActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DcnRemovalTask extends BaseDcnTask {

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DcnRemoveActionEvent event) throws CouldNotRemoveDcnException {
        final String domain = event.getRelatedTo();
        dcnDeployment.removeDcn(domain);
    }

}

package net.geant.nmaas.orchestration.tasks.dcn;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DcnVerificationTask extends BaseDcnTask {

    @EventListener
    @Loggable(LogLevel.INFO)
    public void trigger(DcnVerifyActionEvent event) {
    	try{
	        final String domain = event.getRelatedTo();
	        providersManager.getDcnDeploymentProvider(domain).verifyDcn(domain);
    	} catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }
}
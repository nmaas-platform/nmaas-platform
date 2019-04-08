package net.geant.nmaas.orchestration.tasks.dcn;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.dcn.deployment.repositories.DomainDcnDetailsRepository;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Log4j2
public class DcnRequestVerificationTask extends BaseDcnTask {

    private DomainDcnDetailsRepository repository;

    @Autowired
    public DcnRequestVerificationTask(DomainDcnDetailsRepository repository){
        this.repository = repository;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(DcnVerifyRequestActionEvent event) {
    	try{
	        final String domain = event.getRelatedTo();
	        providersManager.getDcnDeploymentProvider(domain).verifyRequest(domain, constructDcnSpec(domain));
    	} catch(Exception ex){
            long timestamp = System.currentTimeMillis();
            log.error("Error reported at " + timestamp, ex);
        }
    }

    private DcnSpec constructDcnSpec(String domainName) {
        DomainDcnDetails domainDcnDetails = repository.findByDomainCodename(domainName).orElseThrow(() -> new IllegalArgumentException("Domain does not exist"));
        return new DcnSpec(buildDcnName(domainName), domainName, domainDcnDetails.getDcnDeploymentType());
    }

    private String buildDcnName(String domain) {
        return domain + "-" + System.nanoTime();
    }
}

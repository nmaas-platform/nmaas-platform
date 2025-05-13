package net.geant.nmaas.orchestration.tasks.dcn;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
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

import java.time.LocalDateTime;

@Component
@Slf4j
public class DcnRequestVerificationTask extends BaseDcnTask {

    private final DomainDcnDetailsRepository repository;

    @Autowired
    public DcnRequestVerificationTask(DcnDeploymentProvidersManager providersManager, DomainDcnDetailsRepository repository) {
        super(providersManager);
        this.repository = repository;
    }

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trigger(DcnVerifyRequestActionEvent event) {
        try {
            final String domain = event.getRelatedTo();
            providersManager.getDcnDeploymentProvider(domain).verifyRequest(domain, constructDcnSpec(domain));
        } catch (Exception ex) {
            log.error("Error reported at {}", LocalDateTime.now(), ex);
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

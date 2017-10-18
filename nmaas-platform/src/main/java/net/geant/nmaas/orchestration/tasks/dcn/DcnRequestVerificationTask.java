package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentMode;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DcnRequestVerificationTask {

    private final static Logger log = LogManager.getLogger(DcnRequestVerificationTask.class);

    private DcnDeploymentProvider dcnDeployment;

    @Autowired
    public DcnRequestVerificationTask(DcnDeploymentProvider dcnDeployment) {
        this.dcnDeployment = dcnDeployment;
    }

    @Value("${dcn.deployment.mode}")
    private String mode;

    @EventListener
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verifyDcnRequest(DcnVerifyRequestActionEvent event) {
        final Identifier clientId = event.getClientId();
        try {
            if (DcnDeploymentMode.AUTO.toString().equals(mode) || DcnDeploymentMode.MANUAL.toString().equals(mode))
                dcnDeployment.verifyRequest(clientId, constructDcnSpec(clientId));
        } catch (DcnRequestVerificationException e) {
            log.warn("DCN request verification failed for client " + clientId.value() + " -> " + e.getMessage());
        }
    }

    @Loggable(LogLevel.DEBUG)
    public DcnSpec constructDcnSpec(Identifier clientId) {
        return new DcnSpec(buildDcnName(clientId), clientId);
    }

    @Loggable(LogLevel.DEBUG)
    public String buildDcnName(Identifier clientId) {
        return clientId + "-" + System.nanoTime();
    }

}

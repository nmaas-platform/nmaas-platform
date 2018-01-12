package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class BaseDcnTask {

    @Autowired
    protected DcnDeploymentProvider dcnDeployment;

    @Value("${dcn.deployment.mode}")
    protected String mode;

}

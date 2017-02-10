package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.DcnDeploymentCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/api/dcns")
public class AnsibleNotificationRestController {

    private DcnDeploymentCoordinator coordinator;

    @Autowired
    public AnsibleNotificationRestController(DcnDeploymentCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String basicInfo() {
        return "This is NMaaS Platform REST API for DCN configuration";
    }

    @RequestMapping(value = "/notifications/{encodedServiceId}/status", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void notifyDcnConfigurationStatus(@PathVariable String encodedServiceId, @RequestBody AnsiblePlaybookStatus input) {
        coordinator.notifyPlaybookExecutionState(encodedServiceId, input.convertedStatus());
    }

}

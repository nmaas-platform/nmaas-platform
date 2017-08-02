package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.AnsiblePlaybookExecutionStateListener;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/dcns/notifications")
public class AnsibleNotificationRestController {

    private AnsiblePlaybookExecutionStateListener stateListener;

    @Autowired
    public AnsibleNotificationRestController(AnsiblePlaybookExecutionStateListener stateListener) {
        this.stateListener = stateListener;
    }

    @RequestMapping(value = "/{encodedPlaybookId}/status", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void notifyDcnConfigurationStatus(@PathVariable String encodedPlaybookId, @RequestBody AnsiblePlaybookStatus input) {
        stateListener.notifyPlaybookExecutionState(encodedPlaybookId, input.convertedStatus());
    }

}

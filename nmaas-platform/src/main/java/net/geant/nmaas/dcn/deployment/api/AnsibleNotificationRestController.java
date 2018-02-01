package net.geant.nmaas.dcn.deployment.api;

import net.geant.nmaas.dcn.deployment.AnsiblePlaybookExecutionStateListener;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Defines endpoint for receiving notifications about DCN configuration status.
 * Presumably to be invoked at the end of Ansible playbook execution.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@Profile("dcn_ansible")
@RequestMapping(value = "/platform/api/dcns/notifications")
public class AnsibleNotificationRestController {

    private AnsiblePlaybookExecutionStateListener stateListener;

    @Autowired
    public AnsibleNotificationRestController(AnsiblePlaybookExecutionStateListener stateListener) {
        this.stateListener = stateListener;
    }

    @PostMapping(value = "/{encodedPlaybookId}/status", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void notifyDcnConfigurationStatus(@PathVariable String encodedPlaybookId, @RequestBody AnsiblePlaybookStatus input) {
        stateListener.notifyPlaybookExecutionState(encodedPlaybookId, input.convertedStatus());
    }

}

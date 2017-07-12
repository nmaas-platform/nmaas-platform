package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.api.AnsibleNotificationRestController;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;

/**
 * Defines a method to be used by {@link AnsibleNotificationRestController} upon reception of a Ansible playbook
 * execution status notification API call.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AnsiblePlaybookExecutionStateListener {

    /**
     * Method used to notify Ansible playbook execution status.
     *
     * @param encodedPlaybookIdentifier encoded identifier of the playbook being executed
     * @param status Ansible playbook execution status
     */
    void notifyPlaybookExecutionState(String encodedPlaybookIdentifier, AnsiblePlaybookStatus.Status status);

}

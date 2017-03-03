package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface AnsiblePlaybookExecutionStateListener {

    void notifyPlaybookExecutionState(String encodedServiceId, AnsiblePlaybookStatus.Status status);

}

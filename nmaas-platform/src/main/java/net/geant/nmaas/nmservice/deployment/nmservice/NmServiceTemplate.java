package net.geant.nmaas.nmservice.deployment.nmservice;

import java.io.Serializable;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceTemplate extends Serializable {

    Boolean verify();

    Boolean verifyNmServiceSpec(NmServiceSpec spec);

}

package net.geant.nmaas.nmservice.deployment.nmservice;

import net.geant.nmaas.orchestration.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceTemplate {

    Identifier getApplicationId();

    String getName();

    Boolean verify();

    Boolean verifyNmServiceSpec(NmServiceSpec spec);

}

package net.geant.nmaas.servicedeployment.nmservice;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceSpec {

    public String getName();

    public Boolean verify();

}

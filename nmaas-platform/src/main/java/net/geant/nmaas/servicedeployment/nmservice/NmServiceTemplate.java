package net.geant.nmaas.servicedeployment.nmservice;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceTemplate {

    public String getName();

    public Boolean verify();

    public Boolean verifyNmServiceSpec(NmServiceSpec spec);

}

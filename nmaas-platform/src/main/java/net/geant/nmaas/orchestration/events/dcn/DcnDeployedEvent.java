package net.geant.nmaas.orchestration.events.dcn;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnDeployedEvent extends DcnBaseEvent {

    public DcnDeployedEvent(Object source, String domain) {
        super(source, domain);
    }

}

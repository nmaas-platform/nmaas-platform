package net.geant.nmaas.orchestration.events.dcn;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnDeployActionEvent extends DcnBaseEvent {

    public DcnDeployActionEvent(Object source, String domain) {
        super(source, domain);
    }

}

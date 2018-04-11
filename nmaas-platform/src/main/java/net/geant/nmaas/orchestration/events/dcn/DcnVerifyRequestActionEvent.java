package net.geant.nmaas.orchestration.events.dcn;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnVerifyRequestActionEvent extends DcnBaseEvent {

    public DcnVerifyRequestActionEvent(Object source, String domain) {
        super(source, domain);
    }

}

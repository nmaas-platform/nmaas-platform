package net.geant.nmaas.orchestration.events.dcn;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnVerifyActionEvent extends DcnBaseEvent {

    public DcnVerifyActionEvent(Object source, String domain) {
        super(source, domain);
    }

}

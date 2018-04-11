package net.geant.nmaas.orchestration.events.dcn;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnRemoveActionEvent extends DcnBaseEvent {

    public DcnRemoveActionEvent(Object source, String domain) {
        super(source, domain);
    }

}

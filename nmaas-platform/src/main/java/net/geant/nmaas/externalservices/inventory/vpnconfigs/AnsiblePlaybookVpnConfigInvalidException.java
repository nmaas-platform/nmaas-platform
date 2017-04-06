package net.geant.nmaas.externalservices.inventory.vpnconfigs;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigInvalidException extends Throwable {
    public AnsiblePlaybookVpnConfigInvalidException(String message) {
        super(message);
    }
}

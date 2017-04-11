package net.geant.nmaas.externalservices.inventory.vpnconfigs;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigExistsException extends Exception {
    public AnsiblePlaybookVpnConfigExistsException(String message) {
        super(message);
    }
}

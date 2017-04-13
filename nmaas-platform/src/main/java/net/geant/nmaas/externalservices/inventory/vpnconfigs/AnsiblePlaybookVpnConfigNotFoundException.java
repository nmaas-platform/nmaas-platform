package net.geant.nmaas.externalservices.inventory.vpnconfigs;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigNotFoundException extends Exception {
    public AnsiblePlaybookVpnConfigNotFoundException(String message) {
        super(message);
    }
}

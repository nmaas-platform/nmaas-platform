package net.geant.nmaas.nmservice.configuration.ssh;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class BasicCredentials {

    private final String username;

    private final String password;

    public BasicCredentials(String username) {
        this.username = username;
        this.password = null;
    }

    public BasicCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

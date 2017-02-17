package net.geant.nmaas.nmservice.configuration.ssh;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class BasicCredentials {

    private final String username;

    private final String password;

    public BasicCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String generateHash() {
        return DatatypeConverter.printBase64Binary((username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

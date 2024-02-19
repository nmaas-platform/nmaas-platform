package net.geant.nmaas.utils.ssh;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BasicCredentials {

    private final String username;

    private final String password;

    public BasicCredentials(String username) {
        this.username = username;
        this.password = null;
    }

}

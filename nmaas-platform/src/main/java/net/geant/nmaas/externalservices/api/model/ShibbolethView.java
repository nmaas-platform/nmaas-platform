package net.geant.nmaas.externalservices.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShibbolethView {
    private Long id;
    private String loginUrl;
    private String logoutUrl;
    private boolean allowsBasic;
    private String key;
    private int timeout;

    public ShibbolethView(String loginUrl, String logoutUrl, boolean allowsBasic, String key, int timeout){
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
        this.allowsBasic = allowsBasic;
        this.key = key;
        this.timeout = timeout;
    }
}

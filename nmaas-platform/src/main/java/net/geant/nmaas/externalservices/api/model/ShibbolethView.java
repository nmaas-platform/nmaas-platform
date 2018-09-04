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
}

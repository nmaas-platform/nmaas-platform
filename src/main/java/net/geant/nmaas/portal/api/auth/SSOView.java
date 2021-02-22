package net.geant.nmaas.portal.api.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SSOView {
    private String loginUrl;
    private String logoutUrl;
}

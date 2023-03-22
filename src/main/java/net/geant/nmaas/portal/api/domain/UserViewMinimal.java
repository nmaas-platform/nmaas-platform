package net.geant.nmaas.portal.api.domain;

import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserViewMinimal extends UserBase implements Serializable {

    protected String firstname;
    protected String lastname;

    @Builder.Default
    private Set<UserRoleView> roles = new HashSet<>();
}

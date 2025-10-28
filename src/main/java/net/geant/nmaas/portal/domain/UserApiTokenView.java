package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserApiTokenView {

    private Long id;

    private String name;

    private String tokenValue;

    private boolean valid;

    private boolean deleted;
}

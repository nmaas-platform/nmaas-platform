package net.geant.nmaas.portal.api.user;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.User;


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

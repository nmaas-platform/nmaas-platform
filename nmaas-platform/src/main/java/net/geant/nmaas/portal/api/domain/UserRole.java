package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Role;

@Setter
@Getter
public class UserRole extends DomainAware {
	@NotNull
	Role role;
}

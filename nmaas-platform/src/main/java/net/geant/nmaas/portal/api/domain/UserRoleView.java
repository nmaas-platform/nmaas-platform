package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Role;

@Setter
@Getter
@NoArgsConstructor
public class UserRoleView extends DomainAware {

    @NotNull
	Role role;

	public UserRoleView(Role role, Long domainId){
		super(domainId);
		this.role = role;
	}
}

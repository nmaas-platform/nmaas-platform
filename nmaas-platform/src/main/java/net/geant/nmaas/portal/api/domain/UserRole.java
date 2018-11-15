package net.geant.nmaas.portal.api.domain;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Role;

@Setter
@Getter
@NoArgsConstructor
public class UserRole extends DomainAware {
	@NotNull
	Role role;

	public UserRole(Role role, Long domainId){
		super(domainId);
		this.role = role;
	}
}

package net.geant.nmaas.portal.domain.converters;

import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.UserRole;

public class RoleInverseConverter extends AbstractConverter<UserRole, Role> {

	@Override
	protected Role convert(UserRole source) {
		if(source != null)
			return source.getRole();
		return null;
	}

}

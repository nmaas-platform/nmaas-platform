package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.UserRole;

public class RoleInverseConverter extends AbstractConverter<UserRole, Role> {

	@Override
	protected Role convert(UserRole source) {
		if(source != null)
			return source.getRole();
		return null;
	}

}

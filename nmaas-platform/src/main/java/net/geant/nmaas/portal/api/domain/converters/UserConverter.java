package net.geant.nmaas.portal.api.domain.converters;

import java.util.Set;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.domain.UserRole;
import net.geant.nmaas.portal.persistent.entity.User;
import org.modelmapper.AbstractConverter;

public class UserConverter extends AbstractConverter<User, net.geant.nmaas.portal.api.domain.User> {

    @Override
    protected net.geant.nmaas.portal.api.domain.User convert(User source) {
        return new net.geant.nmaas.portal.api.domain.User(source.getId(), source.getUsername(), convertUserRole(source), source.getSamlToken() != null && !source.getSamlToken().isEmpty());
    }

    private Set<UserRole> convertUserRole(User source){
        return source.getRoles().stream()
                .map(role -> new UserRole(role.getRole(), role.getDomain().getId()))
                .collect(Collectors.toSet());
    }
}

package net.geant.nmaas.portal.api.domain.converters;

import java.util.Set;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.domain.UserRole;
import net.geant.nmaas.portal.persistent.entity.User;
import org.modelmapper.AbstractConverter;

public class UserConverter extends AbstractConverter<User, net.geant.nmaas.portal.api.domain.User> {

    @Override
    protected net.geant.nmaas.portal.api.domain.User convert(User source) {
        return net.geant.nmaas.portal.api.domain.User.builder()
                .id(source.getId())
                .username(source.getUsername())
                .firstname(source.getFirstname())
                .lastname(source.getLastname())
                .email(source.getEmail())
                .roles(convertUserRole(source))
                .ssoUser(source.getSamlToken() != null && !source.getSamlToken().isEmpty())
                .build();
    }

    private Set<UserRole> convertUserRole(User source){
        return source.getRoles().stream()
                .map(role -> new UserRole(role.getRole(), role.getDomain().getId()))
                .collect(Collectors.toSet());
    }
}
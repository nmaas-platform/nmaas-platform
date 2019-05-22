package net.geant.nmaas.portal.api.domain.converters;

import java.util.Set;
import java.util.stream.Collectors;
import net.geant.nmaas.portal.api.domain.UserRoleView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.User;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.AbstractConverter;

public class UserConverter extends AbstractConverter<User, UserView> {

    @Override
    protected UserView convert(User source) {
        return UserView.builder()
                .id(source.getId())
                .username(source.getUsername())
                .firstname(source.getFirstname())
                .lastname(source.getLastname())
                .email(source.getEmail())
                .enabled(source.isEnabled())
                .roles(convertUserRole(source))
                .ssoUser(StringUtils.isNotEmpty(source.getSamlToken()))
                .selectedLanguage(source.getSelectedLanguage())
                .build();
    }

    private Set<UserRoleView> convertUserRole(User source){
        return source.getRoles().stream()
                .map(role -> new UserRoleView(role.getRole(), role.getDomain().getId()))
                .collect(Collectors.toSet());
    }
}

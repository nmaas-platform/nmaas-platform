package net.geant.nmaas.portal.api.domain.converters;

import net.geant.nmaas.portal.api.domain.SSHKeyView;
import net.geant.nmaas.portal.api.domain.UserRoleView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

import java.util.Set;
import java.util.stream.Collectors;

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
                .defaultDomain(source.getDefaultDomain())
                .roles(convertUserRole(source))
                .ssoUser(StringUtils.isNotEmpty(source.getSamlToken()))
                .selectedLanguage(source.getSelectedLanguage())
                .sshKeys(convertSshKeys(source))
                .build();
    }

    private Set<UserRoleView> convertUserRole(User source){
        return source.getRoles().stream()
                .map(role -> new UserRoleView(role.getRole(), role.getDomain().getId()))
                .collect(Collectors.toSet());
    }

    private Set<SSHKeyView> convertSshKeys(User source) {
        return source.getSshKeys().stream()
                .map(key -> new SSHKeyView(key.getId(), key.getName(), key.getFingerprint()))
                .collect(Collectors.toSet());
    }

}

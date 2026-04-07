package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.SSHKeyView;
import net.geant.nmaas.api.dto.users.UserDto;
import net.geant.nmaas.api.dto.users.UserRoleDto;
import net.geant.nmaas.portal.persistence.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

import java.util.Set;
import java.util.stream.Collectors;

public class UserConverter extends AbstractConverter<User, UserDto> {

    @Override
    protected UserDto convert(User source) {
        return UserDto.builder()
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

    private Set<UserRoleDto> convertUserRole(User source) {
        return source.getRoles().stream()
                .map(role -> new UserRoleDto(RoleDto.valueOf(role.getRole().name()), role.getDomain().getId(), role.getDomain().getName()))
                .collect(Collectors.toSet());
    }

    private Set<SSHKeyView> convertSshKeys(User source) {
        return source.getSshKeys().stream()
                .map(key -> new SSHKeyView(key.getId(), key.getName(), key.getFingerprint()))
                .collect(Collectors.toSet());
    }

}

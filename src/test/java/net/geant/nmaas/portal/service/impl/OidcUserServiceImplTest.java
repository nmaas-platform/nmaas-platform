package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exception.ExternalUserMatchException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OidcUserServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private DomainService domainService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private OidcUserServiceImpl oidcUserService;

    @BeforeEach
    void setUp() {
        when(oidcUser.getAttribute("sub")).thenReturn("test-sub");
        when(oidcUser.getAttribute("email")).thenReturn("test@example.com");
        when(oidcUser.getAttribute("preferred_username")).thenReturn("testuser");
    }

    @Test
    void shouldReturnExistingUserWhenSamlTokenExistsAndIsValid() {
        //given
        User existingUser = new User("testuser");
        //when
        when(userService.existsBySamlToken("test-sub")).thenReturn(true);
        when(userService.findBySamlToken("test-sub")).thenReturn(Optional.of(existingUser));
        User result = oidcUserService.checkUser(oidcUser);
        //then
        assertEquals(existingUser, result);
    }

    @Test
    void shouldReturnExistingUserWhenSamlTokenExistsAndIsUsername() {
        //given
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setSamlToken("test@example.com");
        //when
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        User result = oidcUserService.checkUser(oidcUser);
        //then
        assertEquals(existingUser, result);
    }



}
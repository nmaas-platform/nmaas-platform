package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationControllerTest {

    private UserService userService = mock(UserService.class);

    private DomainService domainService = mock(DomainService.class);

    private ModelMapper modelMapper = new ModelMapper();

    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private RegistrationController registrationController;

    private Registration registration;

    private static final Domain GLOBAL_DOMAIN = new Domain(1L,"global", "global", true);

    private static final Domain DOMAIN = new Domain(2L,"testdom", "testdom", true);

    @BeforeEach
    public void setup(){
        registration = this.createRegistration();
        registrationController = new RegistrationController(userService, domainService, modelMapper, eventPublisher);
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(GLOBAL_DOMAIN));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(userService.register(any(), any(), any())).thenReturn(new User("test"));
        when(domainService.getDomains()).thenReturn(Arrays.asList(GLOBAL_DOMAIN, DOMAIN));
    }

    @Test
    public void shouldSignupWithoutAnyDomainSelected(){
        this.registrationController.signup(registration, "token");
        verify(userService, times(1)).register(any(), any(), any());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    public void shouldSignupWithSelectedDomain(){
        registration.setDomainId(DOMAIN.getId());
        this.registrationController.signup(registration, "token");
        verify(userService, times(1)).register(any(), any(), any());
        verify(eventPublisher, times(1)).publishEvent(any());
        verify(domainService, times(1)).addMemberRole(any(), any(), any());
    }

    @Test
    public void shouldNotSignupWhenRegistrationIsNull(){
        assertThrows(SignupException.class, () -> {
            registrationController.signup(null, "token");
        });
    }

    @Test
    public void shouldNotSignupWhenUserHasEmptyUsername(){
        assertThrows(SignupException.class, () -> {
            registration.setUsername("");
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldNotSignupWhenUserHasEmptyPassword(){
        assertThrows(SignupException.class, () -> {
            registration.setPassword(null);
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldNotSignupWhenUserHasEmptyMail(){
        assertThrows(SignupException.class, () -> {
            registration.setEmail(null);
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldNotSignupWhenUserNotAcceptTermsOfUse(){
        assertThrows(SignupException.class, () -> {
            registration.setTermsOfUseAccepted(false);
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldNotSignupWhenUserNotAcceptPrivacyPolicy(){
        assertThrows(SignupException.class, () -> {
            registration.setPrivacyPolicyAccepted(false);
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldNotSignupWithWrongDomainId(){
        assertThrows(SignupException.class, () -> {
            registration.setDomainId(9L);
            when(domainService.findDomain(registration.getDomainId())).thenReturn(Optional.empty());
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldNotSignupWithoutGlobalDomain(){
        assertThrows(MissingElementException.class, () -> {
            when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
            registrationController.signup(registration, "token");
        });
    }

    @Test
    public void shouldGetDomains(){
        List<DomainView> result = registrationController.getDomains();
        assertEquals(1, result.size());
        assertEquals(DOMAIN.getCodename(), result.get(0).getCodename());
    }

    @Test
    public void shouldNotGetDomainsWhenGlobalIsMissing(){
        assertThrows(MissingElementException.class, () -> {
            when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
            when(domainService.getDomains()).thenReturn(Collections.singletonList(DOMAIN));
            registrationController.getDomains();
        });
    }

    private Registration createRegistration(){
        Registration registration = new Registration("test");
        registration.setPassword("secret");
        registration.setEmail("test@test.com");
        registration.setTermsOfUseAccepted(true);
        registration.setPrivacyPolicyAccepted(true);
        return registration;
    }

}

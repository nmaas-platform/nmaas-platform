package net.geant.nmaas.portal.api.market;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

public class RegistrationControllerTest {

    private UserService userService = mock(UserService.class);

    private DomainService domainService = mock(DomainService.class);

    private ModelMapper modelMapper = new ModelMapper();

    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private RegistrationController registrationController;

    private Registration registration;

    private static final Domain GLOBAL_DOMAIN = new Domain(1L,"global", "global", true, "namespace", "storage");

    private static final Domain DOMAIN = new Domain(2L,"testdom", "testdom", true,  "namespace", "storage");

    @Before
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
        this.registrationController.signup(registration);
        verify(userService, times(1)).register(any(), any(), any());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    public void shouldSignupWithSelectedDomain(){
        registration.setDomainId(DOMAIN.getId());
        this.registrationController.signup(registration);
        verify(userService, times(1)).register(any(), any(), any());
        verify(eventPublisher, times(1)).publishEvent(any());
        verify(domainService, times(1)).addMemberRole(any(), any(), any());
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWhenRegistrationIsNull(){
        registrationController.signup(null);
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWhenUserHasEmptyUsername(){
        registration.setUsername("");
        registrationController.signup(registration);
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWhenUserHasEmptyPassword(){
        registration.setPassword(null);
        registrationController.signup(registration);
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWhenUserHasEmptyMail(){
        registration.setEmail(null);
        registrationController.signup(registration);
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWhenUserNotAcceptTermsOfUse(){
        registration.setTermsOfUseAccepted(false);
        registrationController.signup(registration);
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWhenUserNotAcceptPrivacyPolicy(){
        registration.setPrivacyPolicyAccepted(false);
        registrationController.signup(registration);
    }

    @Test(expected = SignupException.class)
    public void shouldNotSignupWithWrongDomainId(){
        registration.setDomainId(9L);
        when(domainService.findDomain(registration.getDomainId())).thenReturn(Optional.empty());
        registrationController.signup(registration);
    }

    @Test(expected = MissingElementException.class)
    public void shouldNotSignupWithoutGlobalDomain(){
        when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
        registrationController.signup(registration);
    }

    @Test
    public void shouldGetDomains(){
        List<DomainView> result = registrationController.getDomains();
        assertEquals(1, result.size());
        assertEquals(DOMAIN.getCodename(), result.get(0).getCodename());
    }

    @Test(expected = MissingElementException.class)
    public void shouldNotGetDomainsWhenGlobalIsMissing(){
        when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
        when(domainService.getDomains()).thenReturn(Collections.singletonList(DOMAIN));
        registrationController.getDomains();
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

package net.geant.nmaas.portal.api.domains;

import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.domains.DomainBaseWithStateDto;
import net.geant.nmaas.api.dto.domains.DomainDto;
import net.geant.nmaas.api.dto.domains.DomainRequest;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainControllerTest {

    private final ModelMapper modelMapper = mock(ModelMapper.class);
    private final UserService userService = mock(UserService.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ApplicationStatePerDomainService appStatePerDomainService = mock(ApplicationStatePerDomainService.class);
    private final ApplicationInstanceService appInstanceService = mock(ApplicationInstanceService.class);

    private final DomainController controller = new DomainController(
            modelMapper,
            userService,
            domainService,
            eventPublisher,
            appStatePerDomainService,
            appInstanceService
    );

    @Test
    void shouldGetDomainsAndMapWithGroupState() {
        Domain domain = new Domain(1L, "d1", "d1", true);
        DomainDto mapped = new DomainDto();
        when(domainService.getDomains()).thenReturn(List.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(domain, DomainDto.class)).thenReturn(mapped);

        List<DomainDto> result = controller.getDomains(org.springframework.data.domain.PageRequest.of(0, 10), null, false);

        assertEquals(1, result.size());
        assertEquals(mapped, result.getFirst());
        verify(domainService).getAppStatesFromGroups(domain);
    }

    @Test
    void shouldGetDomainsBasePaginatedAndNonPaginated() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        var page = new org.springframework.data.domain.PageImpl<>(List.of(mock(DomainBaseDto.class)));
        List<DomainBaseDto> list = List.of(mock(DomainBaseDto.class));
        when(domainService.getDomainsBase(pageable, "test")).thenReturn(page);
        when(domainService.getDomainsBase("test")).thenReturn(list);

        ResponseEntity<?> paginated = controller.getDomainsBase(pageable, "test", true);
        ResponseEntity<?> notPaginated = controller.getDomainsBase(pageable, "test", false);

        assertEquals(page, paginated.getBody());
        assertEquals(list, notPaginated.getBody());
    }

    @Test
    void shouldReturnFullDomainViewForSystemAdmin() {
        Principal principal = () -> "admin";
        User admin = new User("admin", true);
        Domain global = new Domain(1L, "global", "global", true);
        admin.setRoles(List.of(new UserRole(admin, global, Role.ROLE_SYSTEM_ADMIN)));
        Domain domain = new Domain(2L, "domain", "domain", true);
        DomainDto full = new DomainDto();
        when(userService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(domainService.findDomain(2L)).thenReturn(Optional.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(domain, DomainDto.class)).thenReturn(full);

        DomainBaseDto result = controller.getDomain(2L, principal);

        assertEquals(full, result);
    }

    @Test
    void shouldReturnBaseDomainViewForRegularUser() {
        Principal principal = () -> "user";
        User user = new User("user", true);
        Domain domainUser = new Domain(10L, "d10", "d10", true);
        user.setRoles(List.of(new UserRole(user, domainUser, Role.ROLE_USER)));
        Domain domain = new Domain(2L, "domain", "domain", true);
        DomainBaseWithStateDto base = new DomainBaseWithStateDto();
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(domainService.findDomain(2L)).thenReturn(Optional.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(domain, DomainBaseWithStateDto.class)).thenReturn(base);

        DomainBaseDto result = controller.getDomain(2L, principal);

        assertEquals(base, result);
    }

    @Test
    void shouldGetMyDomainsAndWrapObjectNotFound() {
        Principal principal = () -> "user1";
        User user = new User("user1", true);
        user.setId(99L);
        Domain domain = new Domain(5L, "d5", "d5", true);
        DomainBaseDto base = new DomainBaseDto();

        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(domainService.getUserDomains(99L, "x")).thenReturn(Set.of(domain));
        when(modelMapper.map(domain, DomainBaseDto.class)).thenReturn(base);

        List<DomainBaseDto> result = controller.getMyDomains(principal, "x");
        assertEquals(1, result.size());

        when(domainService.getUserDomains(99L, "y")).thenThrow(new ObjectNotFoundException("missing"));
        assertThrows(MissingElementException.class, () -> controller.getMyDomains(principal, "y"));
    }

    @Test
    void shouldCreateDomainAndPublishEventsWhenDcnConfigured() {
        DomainRequest request = mock(DomainRequest.class);
        when(request.getName()).thenReturn("new-domain");
        when(domainService.existsDomain("new-domain")).thenReturn(false);

        Domain createdDomain = mock(Domain.class);
        DomainDcnDetails dcn = mock(DomainDcnDetails.class);
        when(createdDomain.getId()).thenReturn(123L);
        when(createdDomain.getCodename()).thenReturn("codename");
        when(createdDomain.getDomainDcnDetails()).thenReturn(dcn);
        when(dcn.getDcnDeploymentType()).thenReturn(DcnDeploymentType.MANUAL);
        when(dcn.isDcnConfigured()).thenReturn(true);
        when(domainService.createDomain(request)).thenReturn(createdDomain);

        Id result = controller.createDomain(request);

        assertNotNull(result);
        verify(domainService).storeDcnInfo("codename", DcnDeploymentType.MANUAL);
        verify(eventPublisher, times(2)).publishEvent(any());
    }

    @Test
    void shouldThrowWhenCreatingAlreadyExistingDomain() {
        DomainRequest request = mock(DomainRequest.class);
        when(request.getName()).thenReturn("dup");
        when(domainService.existsDomain("dup")).thenReturn(true);

        assertThrows(ProcessingException.class, () -> controller.createDomain(request));
    }

    @Test
    void shouldUpdateDcnConfiguredFlagAndPublishProperEvent() {
        Domain domain = mock(Domain.class);
        DomainDcnDetails dcn = mock(DomainDcnDetails.class);
        when(domain.getDomainDcnDetails()).thenReturn(dcn);
        when(domain.getCodename()).thenReturn("dcn-codename");
        when(domainService.changeDcnConfiguredFlag(5L, true)).thenReturn(domain);
        when(dcn.isDcnConfigured()).thenReturn(true);

        Id enabled = controller.updateDcnConfiguredFlag(5L, true);
        assertNotNull(enabled);
        verify(eventPublisher, times(2)).publishEvent(any());

        when(domainService.changeDcnConfiguredFlag(6L, false)).thenReturn(domain);
        when(dcn.isDcnConfigured()).thenReturn(false);
        Id disabled = controller.updateDcnConfiguredFlag(6L, false);
        assertNotNull(disabled);
    }

    @Test
    void shouldThrowOnUpdateDomainWhenIdMismatch() {
        DomainDto update = new DomainDto();
        update.setId(2L);

        assertThrows(ProcessingException.class, () -> controller.updateDomain(1L, update));
    }
}

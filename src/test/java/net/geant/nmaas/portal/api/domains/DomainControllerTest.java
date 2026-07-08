package net.geant.nmaas.portal.api.domains;

import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        DomainDto result = controller.getDomain(2L, principal);

        assertEquals(full, result);
    }

    @Test
    void shouldReturnFullDomainViewForGroupManager() {
        Principal principal = () -> "group-manager";
        User groupManager = new User("group-manager", true);
        Domain global = new Domain(1L, "global", "global", true);
        groupManager.setRoles(List.of(new UserRole(groupManager, global, Role.ROLE_GROUP_MANAGER)));
        Domain domain = domainWithRestrictedData();
        DomainDto full = new DomainDto();
        when(userService.findByUsername("group-manager")).thenReturn(Optional.of(groupManager));
        when(domainService.findDomain(2L)).thenReturn(Optional.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(any(Domain.class), any())).thenReturn(full);

        DomainDto result = controller.getDomain(2L, principal);

        assertEquals(full, result);
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);
        verify(modelMapper).map(domainCaptor.capture(), any());
        assertFullDomainDataAvailable(domainCaptor.getValue());
    }

    @Test
    void shouldReturnRestrictedDomainViewForGroupDomainAdmin() {
        Principal principal = () -> "group-domain-admin";
        User groupDomainAdmin = new User("group-domain-admin", true);
        Domain domain = domainWithRestrictedData();
        groupDomainAdmin.setRoles(List.of(new UserRole(groupDomainAdmin, domain, Role.ROLE_GROUP_DOMAIN_ADMIN)));
        DomainDto dto = new DomainDto();
        when(userService.findByUsername("group-domain-admin")).thenReturn(Optional.of(groupDomainAdmin));
        when(domainService.findDomain(2L)).thenReturn(Optional.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(any(Domain.class), any())).thenReturn(dto);

        DomainDto result = controller.getDomain(2L, principal);

        assertEquals(dto, result);
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);
        verify(modelMapper).map(domainCaptor.capture(), any());
        assertRestrictedDomainDataRemoved(domainCaptor.getValue());
    }

    @Test
    void shouldReturnBaseDomainViewForRegularUser() {
        Principal principal = () -> "user";
        User user = new User("user", true);
        Domain domainUser = new Domain(10L, "d10", "d10", true);
        user.setRoles(List.of(new UserRole(user, domainUser, Role.ROLE_USER)));
        Domain domain = new Domain(2L, "domain", "domain", true);
        domain.setDomainTechDetails(new net.geant.nmaas.orchestration.entities.DomainTechDetails());
        domain.setDomainDcnDetails(new DomainDcnDetails());
        domain.setGroups(List.of(mock(net.geant.nmaas.portal.persistence.entity.DomainGroup.class)));
        domain.setClusters(List.of(mock(net.geant.nmaas.kubernetes.remote.entities.KCluster.class)));
        DomainDto dto = new DomainDto();
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(domainService.findDomain(2L)).thenReturn(Optional.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(any(Domain.class), any())).thenReturn(dto);

        DomainDto result = controller.getDomain(2L, principal);

        assertEquals(dto, result);
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);
        verify(modelMapper).map(domainCaptor.capture(), any());
        assertRestrictedDomainDataRemoved(domainCaptor.getValue());
    }

    @Test
    void shouldReturnRestrictedDomainViewByNameForOperator() {
        Principal principal = () -> "operator";
        User user = new User("operator", true);
        Domain global = new Domain(1L, "global", "global", true);
        user.setRoles(List.of(new UserRole(user, global, Role.ROLE_OPERATOR)));
        Domain domain = new Domain(2L, "domain", "domain", true);
        domain.setDomainTechDetails(new net.geant.nmaas.orchestration.entities.DomainTechDetails());
        domain.setDomainDcnDetails(new DomainDcnDetails());
        domain.setGroups(List.of(mock(net.geant.nmaas.portal.persistence.entity.DomainGroup.class)));
        domain.setClusters(List.of(mock(net.geant.nmaas.kubernetes.remote.entities.KCluster.class)));
        DomainDto dto = new DomainDto();
        when(userService.findByUsername("operator")).thenReturn(Optional.of(user));
        when(domainService.findDomain("domain")).thenReturn(Optional.of(domain));
        when(domainService.getAppStatesFromGroups(domain)).thenReturn(domain);
        when(modelMapper.map(any(Domain.class), any())).thenReturn(dto);

        DomainDto result = controller.getDomainByName("domain", principal);

        assertEquals(dto, result);
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);
        verify(modelMapper).map(domainCaptor.capture(), any());
        assertRestrictedDomainDataRemoved(domainCaptor.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ROLE_SYSTEM_ADMIN", "ROLE_OPERATOR"})
    void shouldGetMyDomainsFromAllDomainsBaseForPrivilegedUsers(Role role) {
        Principal principal = () -> "privileged";
        User user = new User("privileged", true);
        Domain global = new Domain(1L, "global", "global", true);
        user.setRoles(List.of(new UserRole(user, global, role)));
        DomainDto domain = new DomainDto();
        domain.setId(1L);
        domain.setName("global");
        domain.setCodename("global");
        domain.setActive(true);
        List<DomainBaseDto> domains = List.of(domain);

        when(userService.findByUsername("privileged")).thenReturn(Optional.of(user));
        when(domainService.getDomainsBase("search")).thenReturn(domains);

        List<DomainBaseDto> result = controller.getMyDomains(principal, "search");

        assertEquals(domains, result);
        DomainDto resultDomain = assertInstanceOf(DomainDto.class, result.getFirst());
        assertNull(resultDomain.getDomainTechDetails());
        assertNull(resultDomain.getDomainDcnDetails());
        assertNull(resultDomain.getGroups());
        assertNull(resultDomain.getClusters());
        verify(domainService).getDomainsBase("search");
        verify(domainService, times(0)).getUserDomains(any(), any());
        verify(modelMapper, times(0)).map(any(), any());
    }

    private void assertRestrictedDomainDataRemoved(Domain domain) {
        assertNull(domain.getDomainTechDetails());
        assertNull(domain.getDomainDcnDetails());
        assertNull(domain.getGroups());
        assertNull(domain.getClusters());
    }

    private void assertFullDomainDataAvailable(Domain domain) {
        assertNotNull(domain.getDomainTechDetails());
        assertNotNull(domain.getDomainDcnDetails());
        assertNotNull(domain.getGroups());
        assertNotNull(domain.getClusters());
    }

    private Domain domainWithRestrictedData() {
        Domain domain = new Domain(2L, "domain", "domain", true);
        domain.setDomainTechDetails(new net.geant.nmaas.orchestration.entities.DomainTechDetails());
        domain.setDomainDcnDetails(new DomainDcnDetails());
        domain.setGroups(List.of(mock(net.geant.nmaas.portal.persistence.entity.DomainGroup.class)));
        domain.setClusters(List.of(mock(net.geant.nmaas.kubernetes.remote.entities.KCluster.class)));
        return domain;
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
        assertEquals(base, result.getFirst());
        verify(domainService).getUserDomains(99L, "x");
        verify(domainService, times(0)).getDomainsBase("x");

        when(domainService.getUserDomains(99L, "y")).thenThrow(new ObjectNotFoundException("missing"));
        assertThrows(MissingElementException.class, () -> controller.getMyDomains(principal, "y"));
    }

    @Test
    void shouldGetMyDomainsFromActiveDomainsForGroupManager() {
        Principal principal = () -> "group-manager";
        User user = new User("group-manager", true);
        Domain global = new Domain(1L, "global", "global", true);
        user.setRoles(List.of(new UserRole(user, global, Role.ROLE_GROUP_MANAGER)));
        Domain activeDomain = new Domain(2L, "active", "active", true);
        Domain inactiveDomain = new Domain(3L, "inactive", "inactive", false);
        DomainBaseDto activeBase = new DomainBaseDto();

        when(userService.findByUsername("group-manager")).thenReturn(Optional.of(user));
        when(domainService.getDomains("search")).thenReturn(List.of(activeDomain, inactiveDomain));
        when(modelMapper.map(activeDomain, DomainBaseDto.class)).thenReturn(activeBase);

        List<DomainBaseDto> result = controller.getMyDomains(principal, "search");

        assertEquals(1, result.size());
        assertEquals(activeBase, result.getFirst());
        verify(domainService).getDomains("search");
        verify(domainService, times(0)).getDomainsBase("search");
        verify(domainService, times(0)).getUserDomains(any(), any());
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

    @Test
    void shouldAllowGroupManagerToPatchDomainTechDetails() throws Exception {
        Method method = DomainController.class.getMethod("updateDomainTechDetails", Long.class, DomainDto.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertTrue(preAuthorize.value().contains("hasRole('ROLE_GROUP_MANAGER')"));
    }
}

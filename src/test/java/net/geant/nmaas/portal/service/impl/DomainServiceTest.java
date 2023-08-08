package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.api.domain.DomainDcnDetailsView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainTechDetailsView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.portal.service.impl.domains.DefaultCodenameValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class DomainServiceTest {

    DomainServiceImpl.CodenameValidator validator;
    DomainServiceImpl.CodenameValidator namespaceValidator;
    DomainRepository domainRepository = mock(DomainRepository.class);
    DomainTechDetailsRepository domainTechDetailsRepository = mock(DomainTechDetailsRepository.class);
    UserService userService = mock(UserService.class);
    UserRoleRepository userRoleRepo = mock(UserRoleRepository.class);
    DcnRepositoryManager dcnRepositoryManager = mock(DcnRepositoryManager.class);
    ApplicationStatePerDomainService applicationStatePerDomainService = mock(ApplicationStatePerDomainService.class);
    ModelMapper modelMapper = new ModelMapper();

    DomainGroupRepository domainGroupRepository = mock(DomainGroupRepository.class);
    DomainGroupService domainGroupService;

    DomainService domainService;

    @BeforeEach
    void setup() {
        validator = new DefaultCodenameValidator("[a-z-]{2,12}");
        namespaceValidator = new DefaultCodenameValidator("[a-z-]{0,64}");
        domainGroupService = new DomainGroupServiceImpl(domainGroupRepository, applicationStatePerDomainService, modelMapper);
        domainService = new DomainServiceImpl(validator,
                namespaceValidator, domainRepository,
                domainTechDetailsRepository, userService,
                userRoleRepo, dcnRepositoryManager,
                modelMapper, applicationStatePerDomainService, domainGroupService);
        ((DomainServiceImpl) domainService).globalDomain = "GLOBAL";
    }

    @Test
    void shouldCreateGlobalDomain() {
        when(domainRepository.findByName(anyString())).thenReturn(Optional.empty());
        Domain domain = new Domain("GLOBAL", "global");
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createGlobalDomain();
        assertThat("Codename mismatch", result.getCodename().equals("global"));
    }

    @Test
    void shouldGetGlobalDomain() {
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(domainRepository.findByName(anyString())).thenReturn(Optional.of(domain));
        Domain result = this.domainService.createGlobalDomain();
        assertThat("Codename mismatch", result.getCodename().equals("GLOBAL"));
    }

    @Test
    void shouldCreateDomain() {
        String name = "testdomain";
        String codename = "testdom";
        Domain domain = new Domain(name, codename);
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createDomain(new DomainRequest(name, codename, true));
        assertThat("Codenames are not the same" ,result.getCodename().equals(codename));
        assertThat("Active is false", result.isActive());
    }

    @Test
    void shouldNotCreateDomainWithInvalidCodename() {
        assertThrows(ProcessingException.class, () -> {
            String name = "testdomain";
            String codename = "test-domain-too-long";
            Domain domain = new Domain(name, codename);
            when(domainRepository.save(domain)).thenReturn(domain);
            this.domainService.createDomain(new DomainRequest(name, codename, true));
        });
    }

    @Test
    void shouldNotCreateDomainWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            String codename = "test-domain";
            this.domainService.createDomain(new DomainRequest(null, codename,true));
        });
    }

    @Test
    void shouldCreateDomainWithFalseActiveFlag() {
        String name = "testdomain";
        String codename = "testdom";
        Domain domain = new Domain(name, codename, false);
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createDomain(new DomainRequest(name, codename, false));
        assertThat("Codenames are not the same" ,result.getCodename().equals(codename));
        assertThat("Active is false", !result.isActive());
    }

    @Test
    void shouldCreateDomainWithOwnParams() {
        String name = "testdomain";
        String codename = "testdom";
        String kubernetesNamespace = "default-namespace";
        String kubernetesStorageClass = "kub-stor-class";
        Domain domain = new Domain(name, codename, false);
        DomainTechDetails domainTechDetails = new DomainTechDetails(null, codename, null, kubernetesNamespace, kubernetesStorageClass, null);
        DomainDcnDetails domainDcnDetails = new DomainDcnDetails(null, codename, false, DcnDeploymentType.NONE, Collections.emptyList());
        domain.setDomainTechDetails(domainTechDetails);
        domain.setDomainDcnDetails(domainDcnDetails);
        when(domainRepository.save(domain)).thenReturn(domain);
        DomainRequest domainRequest = new DomainRequest(name, codename, false);
        DomainDcnDetailsView domainDcnDetailsView = new DomainDcnDetailsView(null, codename, false, DcnDeploymentType.NONE, Collections.emptyList());
        DomainTechDetailsView domainTechDetailsView = new DomainTechDetailsView(null, codename, null, kubernetesNamespace, kubernetesStorageClass, null);
        domainRequest.setDomainDcnDetails(domainDcnDetailsView);
        domainRequest.setDomainTechDetails(domainTechDetailsView);
        Domain result = this.domainService.createDomain(domainRequest);
        assertThat("Name mismatch", result.getName().equals(name));
        assertThat("Codename mismatch", result.getCodename().equals(codename));
        assertThat("Active flag is incorrect", !result.isActive());
        assertThat("dcnConfigured flag is incorrect", !result.getDomainDcnDetails().isDcnConfigured());
        assertThat("Kubernetes namespace mismatch", result.getDomainTechDetails().getKubernetesNamespace().equals(kubernetesNamespace));
        assertThat("Kubernetes storage class mismatch", result.getDomainTechDetails().getKubernetesStorageClass().equals(kubernetesStorageClass));
    }

    @Test
    void shouldUpdateDomain() {
        String name = "testdomain";
        String codename = "testdom";
        Domain domain = new Domain(1L, name, codename, true);
        domain.setApplicationStatePerDomain(new ArrayList<>());
        domain.setDomainTechDetails(new DomainTechDetails());
        this.domainService.updateDomain(domain);
        verify(domainRepository, times(1)).save(domain);
    }

    @Test
    void shouldNotUpdateNonExistingDomain() {
        assertThrows(ProcessingException.class, () -> {
            String name = "testdomain";
            String codename = "testdom";
            this.domainService.updateDomain(new Domain(name, codename));
        });
    }

    @Test
    void shouldNotUpdateGlobalDomain() {
        assertThrows(IllegalArgumentException.class, () -> {
            String name = "GLOBAL";
            String codename = "GLOBAL";
            this.domainService.updateDomain(new Domain(1L, name, codename));
        });
    }

    @Test
    void shouldNotUpdateNullDomain() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.domainService.updateDomain(null);
        });
    }

    @Test
    void shouldRemoveDomain() {
        Domain domain = new Domain(1L, "testdom", "testdom");
        when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
        assertTrue(this.domainService.removeDomain(1L));
        verify(domainRepository, times(1)).delete(domain);
    }

    @Test
    void shouldNotRemoveGlobalDomain() {
        assertThrows(IllegalArgumentException.class, () -> {
            Domain domain = new Domain(1L, "GLOBAL", "GLOBAL");
            when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
            assertFalse(this.domainService.removeDomain(1L));
        });
    }

    @Test
    void shouldAddMemberRole() {
        Long domainId = 1L;
        Long userId = 1L;
        Role role = Role.ROLE_OPERATOR;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userRoleRepo.findByDomainAndUserAndRole(domain, user, role)).thenReturn(null);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.addMemberRole(domainId, userId, role);
        verify(userRoleRepo, times(1)).save(any());
    }

    @Test
    void shouldNotAddMemberRoleWithNullDomainId() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.domainService.addMemberRole(null, 1L, Role.ROLE_OPERATOR);
        });
    }

    @Test
    void shouldNotAddMemberRoleWithNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.domainService.addMemberRole(1L, null, Role.ROLE_OPERATOR);
        });
    }

    @Test
    void shouldNotAddMemberRoleWithEmptyRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.domainService.addMemberRole(1L, 1L, null);
        });
    }

    @Test
    void shouldRemoveMemberRole() {
        Long domainId = 1L;
        Long userId = 1L;
        Role role = Role.ROLE_OPERATOR;
        Domain domain = new Domain(domainId, "testdom", "testdom", true);
        User user = new User("user");
        user.setId(userId);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.removeMemberRole(domainId, userId, role);
        verify(userRoleRepo, times(1)).deleteBy(user.getId(), domain.getId(), role);
    }

    @Test
    void shouldRemoveMember() {
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom", true);
        User user = new User("user");
        user.setId(userId);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.removeMember(domainId, userId);
        verify(userRoleRepo, times(1)).deleteBy(user.getId(), domain.getId());
    }

    @Test
    void shouldGetMemberRoles() {
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom", true);
        User user = new User("user");
        user.setId(userId);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(userRoleRepo.findRolesByDomainAndUser(domain.getId(), user.getId())).thenReturn(ImmutableSet.of(Role.ROLE_SYSTEM_ADMIN));
        Set<Role> roleSet = this.domainService.getMemberRoles(domainId, userId);
        assertThat("Result set mismatch", roleSet.equals(ImmutableSet.of(Role.ROLE_SYSTEM_ADMIN)));
    }

    @Test
    void shouldGetMember() {
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom", true);
        User user = new User("user");
        user.setId(userId);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(userRoleRepo.findDomainMember(domain.getId(), user.getId())).thenReturn(Optional.of(user));
        User result = this.domainService.getMember(domainId, userId);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void shouldNotGetMember() {
        assertThrows(ProcessingException.class, () -> {
            Long domainId = 1L;
            Long userId = 1L;
            Domain domain = new Domain(domainId, "testdom", "testdom", true);
            User user = new User("user");
            when(userService.findById(userId)).thenReturn(Optional.of(user));
            when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
            when(userRoleRepo.findDomainMember(domain.getId(), user.getId())).thenReturn(Optional.empty());
            this.domainService.getMember(domainId, userId);
        });
    }

    @Test
    void shouldNotGetMemberWithEmptyUser() {
        assertThrows(ProcessingException.class, () -> {
            Long domainId = 1L;
            Domain domain = new Domain(domainId, "testdom", "testdom");
            when(userService.findById(1L)).thenReturn(Optional.empty());
            when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
            this.domainService.getMember(domainId, 1L);
        });
    }

    @Test
    void shouldThrowAnExceptionWhenGetMemberWithNullDomain() {
        assertThrows(ProcessingException.class, () -> {
            Long userId = 1L;
            User user = new User("user");
            when(userService.findById(userId)).thenReturn(Optional.of(user));
            when(domainRepository.findById(1L)).thenReturn(Optional.empty());
            this.domainService.getMember(1L, userId);
        });
    }

    @Test
    void shouldGetUserDomains() {
        Long userId = 1L;
        User user = new User("user");
        Domain domain = new Domain(1L, "testdom", "testdom");
        user.setNewRoles(ImmutableSet.of(new UserRole(user, domain, Role.ROLE_SYSTEM_ADMIN)));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        Set<Domain> result = this.domainService.getUserDomains(userId);
        assertThat("Result mismatch", result.contains(domain));
    }

    @Test
    void  shouldFindUsersWithDomainAdminRole() {
        Domain domain = new Domain(1L, "testdom", "testdom");

        User user1 = new User("user1");
        user1.setRoles(ImmutableList.of(new UserRole(user1, domain, Role.ROLE_DOMAIN_ADMIN), new UserRole(user1, domain, Role.ROLE_OPERATOR)));

        User user2 = new User("user2");
        user2.setRoles(ImmutableList.of(new UserRole(user2, domain, Role.ROLE_DOMAIN_ADMIN)));

        List<User> users = ImmutableList.of(user1, user2);
        when(userRoleRepo.findDomainMembers(anyString())).thenReturn(users);
        List<UserView> filteredUsers = domainService.findUsersWithDomainAdminRole(domain.getCodename());
        assertThat("Result mismatch", filteredUsers.size() == 2);
    }

    @Test
    void shouldSoftRemoveDomain() {
        Domain domain = new Domain(1L, "testdom", "testdom");
        when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
        domainService.softRemoveDomain(1L);
        verify(domainRepository, times(1)).save(domain);
        Optional<Domain> deletedDomain = domainService.findDomain(1L);

        assertTrue(deletedDomain.isPresent());
        assertTrue(deletedDomain.get().isDeleted());
        assertTrue(deletedDomain.get().getName().contains("DELETED"));
    }
    
    @Test
    void shouldRemoveDomainFromAllGroupsOnSoftRemoval() {
        Domain domain = new Domain(1L, "testdom", "testdom");
        when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
        DomainGroup group = new DomainGroup(1L, "group1", "g1");
        group.setDomains(new ArrayList<>());
        group.addDomain(domain);
        when(domainGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(domainGroupRepository.save(group)).thenReturn(group);
        domainService.softRemoveDomain(1L);
        var resultDomainGroup = domainGroupRepository.findById(1L);
        assertTrue(resultDomainGroup.isPresent());
        assertTrue(resultDomainGroup.get().getDomains().isEmpty());
    }

}

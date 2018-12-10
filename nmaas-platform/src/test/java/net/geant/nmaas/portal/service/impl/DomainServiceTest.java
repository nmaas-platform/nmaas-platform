package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.portal.service.impl.domains.DefaultCodenameValidator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class DomainServiceTest {

    DomainServiceImpl.CodenameValidator validator;

    DomainRepository domainRepository = mock(DomainRepository.class);

    UserService userService = mock(UserService.class);

    UserRoleRepository userRoleRepo = mock(UserRoleRepository.class);

    DcnRepositoryManager dcnRepositoryManager = mock(DcnRepositoryManager.class);

    DomainService domainService;

    @Before
    public void setup(){
        validator = new DefaultCodenameValidator("[\\p{Alnum}_]{2,8}");
        domainService = new DomainServiceImpl(validator, domainRepository, userService, userRoleRepo, dcnRepositoryManager);
        ((DomainServiceImpl) domainService).globalDomain = "GLOBAL";
    }

    @Test
    public void shouldCreateGlobalDomain() throws ProcessingException{
        when(domainRepository.findByName(anyString())).thenReturn(Optional.empty());
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createGlobalDomain();
        assertThat("Codename mismatch", result.getCodename().equals("GLOBAL"));
    }

    @Test
    public void shouldGetGlobalDomain() throws ProcessingException{
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(domainRepository.findByName(anyString())).thenReturn(Optional.of(domain));
        Domain result = this.domainService.createGlobalDomain();
        assertThat("Codename mismatch", result.getCodename().equals("GLOBAL"));
    }

    @Test
    public void shouldCreateDomain() throws ProcessingException{
        String name = "testdomain";
        String codename = "testdom";
        Domain domain = new Domain(name, codename);
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createDomain(name, codename);
        assertThat("Codenames are not the same" ,result.getCodename().equals(codename));
        assertThat("Active is false", result.isActive());
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotCreateDomainWithInvalidCodename() throws ProcessingException{
        String name = "testdomain";
        String codename = "test-domain";
        Domain domain = new Domain(name, codename);
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createDomain(name, codename);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateDomainWithNullName() throws ProcessingException{
        String codename = "test-domain";
        Domain result = this.domainService.createDomain(null, codename);
    }

    @Test
    public void shouldCreateDomainWithFalseActiveFlag() throws ProcessingException{
        String name = "testdomain";
        String codename = "testdom";
        Domain domain = new Domain(name, codename, false);
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createDomain(name, codename, false);
        assertThat("Codenames are not the same" ,result.getCodename().equals(codename));
        assertThat("Active is false", !result.isActive());
    }

    @Test
    public void shouldCreateDomainWithOwnParams() throws ProcessingException{
        String name = "testdomain";
        String codename = "testdom";
        String kubernetesNamespace = "default-namespace";
        String kubernetesStorageClass = "kub-stor-class";
        Domain domain = new Domain(name, codename, false, false, kubernetesNamespace, kubernetesStorageClass);
        when(domainRepository.save(domain)).thenReturn(domain);
        Domain result = this.domainService.createDomain(name, codename, false, false, kubernetesNamespace, kubernetesStorageClass);
        assertThat("Name mismatch", result.getName().equals(name));
        assertThat("Codename mismatch", result.getCodename().equals(codename));
        assertThat("Active flag is incorrect", !result.isActive());
        assertThat("dcnConfigured flag is incorrect", !result.isDcnConfigured());
        assertThat("Kubernetes namespace mismatch", result.getKubernetesNamespace().equals(kubernetesNamespace));
        assertThat("Kubernetes storage class mismatch", result.getKubernetesStorageClass().equals(kubernetesStorageClass));
    }

    @Test
    public void shouldUpdateDomain() throws ProcessingException{
        String name = "testdomain";
        String codename = "testdom";
        Domain domain = new Domain(1L, name, codename, true, "default", "default");
        this.domainService.updateDomain(domain);
        verify(domainRepository, times(1)).save(domain);
    }

    @Test (expected = ProcessingException.class)
    public void shouldNotUpdateNonExistingDomain() throws ProcessingException{
        String name = "testdomain";
        String codename = "testdom";
        this.domainService.updateDomain(new Domain(name, codename));
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldNotUpdateGlobalDomain() throws ProcessingException{
        String name = "GLOBAL";
        String codename = "GLOBAL";
        this.domainService.updateDomain(new Domain(1L, name, codename));
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldNotUpdateNullDomain() throws ProcessingException{
        this.domainService.updateDomain(null);
    }

    @Test
    public void shouldRemoveDomain(){
        Domain domain = new Domain(1L, "testdom", "testdom");
        when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
        assertTrue(this.domainService.removeDomain(1L));
        verify(domainRepository, times(1)).delete(domain);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldNotRemoveGlobalDomain(){
        Domain domain = new Domain(1L, "GLOBAL", "GLOBAL");
        when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
        assertFalse(this.domainService.removeDomain(1L));
    }

    @Test
    public void shouldAddMemberRole() throws ObjectNotFoundException {
        Long domainId = 1L;
        Long userId = 1L;
        Role role = Role.ROLE_OPERATOR;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userRoleRepo.findByDomainAndUserAndRole(domain, user, role)).thenReturn(null);
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.addMemberRole(domainId, userId, role);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddMemberRoleWithNullDomainId() throws ObjectNotFoundException{
        this.domainService.addMemberRole(null, 1L, Role.ROLE_OPERATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddMemberRoleWithNullUserId() throws ObjectNotFoundException{
        this.domainService.addMemberRole(1L, null, Role.ROLE_OPERATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddMemberRoleWithEmptyRole() throws ObjectNotFoundException{
        this.domainService.addMemberRole(1L, 1L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddMemberSystemComponentRole() throws ObjectNotFoundException{
        this.domainService.addMemberRole(1L, 1L, Role.ROLE_SYSTEM_COMPONENT);
    }

    @Test
    public void shouldRemoveMemberRole() throws ObjectNotFoundException {
        Long domainId = 1L;
        Long userId = 1L;
        Role role = Role.ROLE_OPERATOR;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.removeMemberRole(domainId, userId, role);
        verify(userRoleRepo, times(1)).deleteBy(user, domain, role);
    }

    @Test
    public void shouldRemoveMember() throws ObjectNotFoundException{
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.removeMember(domainId, userId);
        verify(userRoleRepo, times(1)).deleteBy(user, domain);
    }

    @Test
    public void shouldGetMemberRoles() throws ObjectNotFoundException{
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(userRoleRepo.findRolesByDomainAndUser(domain, user)).thenReturn(ImmutableSet.of(Role.ROLE_SYSTEM_ADMIN));
        Set<Role> roleSet = this.domainService.getMemberRoles(domainId, userId);
        assertThat("Result set mismatch", roleSet.equals(ImmutableSet.of(Role.ROLE_SYSTEM_ADMIN)));
    }

    @Test
    public void shouldGetMember() throws ProcessingException{
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(userRoleRepo.findDomainMember(domain, user)).thenReturn(user);
        this.domainService.getMember(domainId, userId);
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotGetMember() throws ProcessingException{
        Long domainId = 1L;
        Long userId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        User user = new User("user");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(userRoleRepo.findDomainMember(domain, user)).thenReturn(null);
        this.domainService.getMember(domainId, userId);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void shouldNotGetMemberWithEmptyUser() throws ProcessingException{
        Long domainId = 1L;
        Domain domain = new Domain(domainId, "testdom", "testdom");
        when(userService.findById(1L)).thenReturn(Optional.empty());
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        this.domainService.getMember(domainId, 1L);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void shouldThrowAnExceptionWhenGetMemberWithNullDomain() throws ProcessingException{
        Long userId = 1L;
        User user = new User("user");
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(domainRepository.findById(1L)).thenReturn(Optional.empty());
        this.domainService.getMember(1L, userId);
    }

    @Test
    public void shouldGetUserDomains() throws ObjectNotFoundException{
        Long userId = 1L;
        User user = new User("user");
        Domain domain = new Domain(1L, "testdom", "testdom");
        user.setNewRoles(ImmutableSet.of(new UserRole(user, domain, Role.ROLE_SYSTEM_ADMIN)));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        Set<Domain> result = this.domainService.getUserDomains(userId);
        assertThat("Result mismatch", result.contains(domain));
    }

    @Test
    public void  shouldFindUsersWithDomainAdminRole(){
        Domain domain = new Domain(1L, "testdom", "testdom");

        User user1 = User.builder().roles(ImmutableList.of(
                new UserRole(User.builder().id(1L).build(), domain, Role.ROLE_DOMAIN_ADMIN),
                new UserRole(User.builder().id(2L).build(), domain, Role.ROLE_SYSTEM_COMPONENT))).build();

        User user2 = User.builder().roles(ImmutableList.of(
                new UserRole(User.builder().id(3L).build(), domain, Role.ROLE_DOMAIN_ADMIN))).build();

        List<User> users = ImmutableList.of(user1, user2);
        when(userRoleRepo.findDomainMembers(anyLong())).thenReturn(users);
        List<User> filteredUsers = domainService.findUsersWithDomainAdminRole(1L);
        assertThat("Result mismatch", filteredUsers.size() == 2);
        assertThat(filteredUsers, Matchers.containsInAnyOrder(user1, user2));
    }

}

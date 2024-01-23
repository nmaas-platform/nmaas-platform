package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.CustomerNetwork;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.net.InetAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class DomainControllerIntTest extends BaseControllerTestSetup {

    private static final Long DEF_DOM_ID = 15L;
    private static final String DEF_DOM_NAME = "defdom";

    @MockBean
    private DomainService domainService;

    @MockBean
    private DomainGroupService domainGroupService;

    @MockBean
    private UserService userService;

    @MockBean
    private ApplicationStatePerDomainService applicationStatePerDomainService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ModelMapper modelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal principalMock = mock(Principal.class);


    @BeforeEach
    void setup() {
        mvc = createMVC();
        when(domainService.getDomains()).thenReturn(Arrays.asList(getDefaultDomain(), getGlobalDomain()));
        when(domainService.findDomain(DEF_DOM_ID)).thenReturn(Optional.of(getDefaultDomain()));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(getGlobalDomain()));
        when(domainService.getAppStatesFromGroups(getDefaultDomain())).thenReturn(getDefaultDomain());
        when(domainService.getAppStatesFromGroups(getGlobalDomain())).thenReturn(getGlobalDomain());
    }

    @Test
    void shouldCreateDomain() throws Exception {
        when(domainService.existsDomain(DEF_DOM_NAME)).thenReturn(false);
        when(domainService.createDomain(any())).thenReturn(getDefaultDomain());
        MvcResult result = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomainRequest("test")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldNotCreateDomainWhenNameIsTaken() throws Exception {
        when(domainService.existsDomain("GLOBAL")).thenReturn(true);
        DomainRequest domainRequest = getDefaultDomainRequest("test");
        domainRequest.setCodename("GLOBAL");
        domainRequest.setName("GLOBAL");
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/domains")
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(domainRequest))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotAcceptable());
        });
    }

    @Test
    void shouldUpdateDomain() throws Exception {
        Domain request = getDefaultDomain();
        request.getDomainTechDetails().setKubernetesNamespace("namespace");
        request.setApplicationStatePerDomain(new ArrayList<>());
        MvcResult result = mvc.perform(put("/api/domains/" + request.getId())
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modelMapper.map(request, DomainView.class)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldAddCustomerNetworks() throws Exception {
        Domain request = getDefaultDomain();
        request.setApplicationStatePerDomain(new ArrayList<>());
        request.getDomainDcnDetails().setCustomerNetworks(Collections.singletonList(new CustomerNetwork(null, InetAddress.getByName("1.1.1.1"), 24)));
        mvc.perform(put("/api/domains/" + request.getId())
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modelMapper.map(request, DomainView.class)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(domainService).updateDomain(any());
    }

    @Test
    void shouldNotUpdateDomainWhenIdIsIncorrect() {
        DomainView request = modelMapper.map(getDefaultDomainRequest("test"), DomainView.class);
        request.setId(999L);
        assertDoesNotThrow(() -> {
            mvc.perform(put("/api/domains/" + DEF_DOM_ID)
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotAcceptable());
        });
    }

    @Test
    void shouldUpdateWithExternalServiceDomainSpecified() throws Exception {
        Domain request = getDefaultDomain();
        request.getDomainTechDetails().setExternalServiceDomain("external-domain");
        request.setApplicationStatePerDomain(new ArrayList<>());
        MvcResult result = mvc.perform(put("/api/domains/" + request.getId())
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldUpdateDomainTechDetails() throws Exception {
        Domain request = getDefaultDomain();
        request.getDomainTechDetails().setKubernetesNamespace("namespace");
        request.setApplicationStatePerDomain(new ArrayList<>());
        MvcResult result = mvc.perform(patch("/api/domains/" + request.getId())
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldNotUpdateDomainTechDetailsAsDomainAdmin() throws Exception {
        Domain request = UsersHelper.DOMAIN1;
        DomainTechDetails techDetails = new DomainTechDetails();
        techDetails.setKubernetesNamespace("namespace");
        request.setDomainTechDetails(techDetails);
        request.setApplicationStatePerDomain(new ArrayList<>());
        MvcResult result = mvc.perform(patch("/api/domains/" + request.getId())
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldNotUpdateTechDetailsWithCorruptedId() {
        Domain domain = getDefaultDomain();
        domain.setApplicationStatePerDomain(new ArrayList<>());
        DomainView request = modelMapper.map(domain, DomainView.class);
        long id = request.getId();
        request.setId(123L);
        assertDoesNotThrow(() -> {
            mvc.perform(patch("/api/domains/" + id)
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotAcceptable());
        });
    }

    @Test
    void shouldChangeDomainState() {
        assertDoesNotThrow(() -> {
            mvc.perform(patch("/api/domains/" + DEF_DOM_ID + "/state?active=false")
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldEnableDcnConfiguredFlag() throws Exception {
        when(domainService.changeDcnConfiguredFlag(DEF_DOM_ID, true)).thenReturn(getDefaultDomain());
        MvcResult result = mvc.perform(patch("/api/domains/" + DEF_DOM_ID + "/dcn?configured=true")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(domainService).changeDcnConfiguredFlag(DEF_DOM_ID, true);
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldDisableDcnConfiguredFlag() throws Exception {
        when(domainService.changeDcnConfiguredFlag(DEF_DOM_ID, false)).thenReturn(getDefaultDomain());
        MvcResult result = mvc.perform(patch("/api/domains/" + DEF_DOM_ID + "/dcn?configured=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(domainService).changeDcnConfiguredFlag(DEF_DOM_ID, false);
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldDeleteDomain() {
        when(domainService.removeDomain(DEF_DOM_ID)).thenReturn(true);
        assertDoesNotThrow(() -> {
            mvc.perform(delete("/api/domains/" + DEF_DOM_ID)
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldNotDeleteDomain() {
        assertDoesNotThrow(() -> {
            mvc.perform(delete("/api/domains/" + 1234)
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        });
    }

    @Test
    void shouldGetDomain() throws Exception {
        when(userService.findByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(UsersHelper.ADMIN));
        when(userService.findById(1L)).thenReturn(Optional.of(UsersHelper.ADMIN));
        MvcResult result = mvc.perform(get("/api/domains/{domainId}", DEF_DOM_ID)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView domain = objectMapper.readValue(result.getResponse().getContentAsString(), DomainView.class);
        assertEquals(DEF_DOM_NAME, domain.getName());
    }

    @Test
    void shouldNotGetDomain() {
        when(userService.findByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(UsersHelper.ADMIN));
        when(userService.findById(1L)).thenReturn(Optional.of(UsersHelper.ADMIN));
        when(domainService.findDomain(2345L)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/domains/{domainId}", 2345)
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        });
    }

    @Test
    void shouldGetDomains() throws Exception {
        MvcResult result = mvc.perform(get("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains("GLOBAL"));
    }

    @Test
    void shouldGetMyDomains() throws Exception {
        User user = new User("testUser");
        user.setId(1L);
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));
        when(domainService.getUserDomains(1L)).thenReturn(Sets.newSet(getDefaultDomain(), getGlobalDomain()));
        MvcResult result = mvc.perform(get("/api/domains/my")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains("GLOBAL"));
    }

    @Test
    void shouldGetMyDomainGroupAsVL() throws Exception {
       when(principalMock.getName()).thenReturn("testUser");
        User user = new User("testUser");
        user.setId(1L);
        UserRole userRole = new UserRole(user, getGlobalDomain(), Role.ROLE_VL_DOMAIN_ADMIN);
        user.setRoles(List.of(userRole));
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));

        DomainGroupView group1 = new DomainGroupView();
        group1.setId(1L);
        User user1 = new User("testUser", true);
        user1.setId(1L);
        group1.setManagers(List.of(modelMapper.map(user1, UserViewMinimal.class)));
        DomainGroupView group2 = new DomainGroupView();
        group2.setId(2L);
        User user2 = new User("testUser2", true);
        user2.setId(1L);
        group2.setManagers(List.of((modelMapper.map(user2, UserViewMinimal.class))));

        when(domainGroupService.getAllDomainGroups()).thenReturn(List.of(group1, group2));

        MvcResult result = mvc.perform(get("/api/domains/group")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldGetOneDomainGroupAsVL() throws Exception {
        when(principalMock.getName()).thenReturn("admin");
        User user = new User("admin");
        user.setId(1L);
        UserRole userRole = new UserRole(user, getGlobalDomain(), Role.ROLE_VL_DOMAIN_ADMIN);
        user.setRoles(List.of(userRole));
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));

        DomainGroupView group1 = new DomainGroupView();
        group1.setId(1L);
        User user1 = new User("testUser", true);
        user1.setId(1L);
        group1.setManagers(List.of(modelMapper.map(user1, UserViewMinimal.class)));
        DomainGroupView group2 = new DomainGroupView();
        User user2 = new User("testUser2", true);
        user2.setId(1L);
        group2.setManagers(List.of((modelMapper.map(user2, UserViewMinimal.class))));

        when(domainGroupService.getDomainGroup(any())).thenReturn(group1);

        MvcResult result = mvc.perform(get("/api/domains/group/1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

//    @Test
//    void shouldNotGetOneDomainGroupAsVL() throws Exception {
//        when(principalMock.getName()).thenReturn("testUser");
//        User user = new User("testUser");
//        user.setId(1L);
//        UserRole userRole = new UserRole(user, getGlobalDomain(), Role.ROLE_VL_DOMAIN);
//        user.setRoles(List.of(userRole));
//        when(userService.findByUsername(any())).thenReturn(Optional.of(user));
//
//        DomainGroupView group1 = new DomainGroupView();
//        group1.setId(1L);
//        group1.setAccessUsers(List.of(new UserViewAccess(1L, "testUser", "Test", "USER")));
//        DomainGroupView group2 = new DomainGroupView();
//        group2.setId(2L);
//        group2.setAccessUsers(List.of(new UserViewAccess(2L, "testUser2", "Test2", "USER2")));
//
//        when(domainGroupService.getDomainGroup(any())).thenReturn(group1);
//
//        ResultActions result = mvc.perform(get("/api/domains/group/2")
//                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().is5xxServerError());
//    }


    private Domain getGlobalDomain() {
        return new Domain(1L, "GLOBAL", "GLOBAL", true);
    }

    private Domain getDefaultDomain(){
        Domain domain = new Domain(DEF_DOM_ID, DEF_DOM_NAME, DEF_DOM_NAME, true);
        domain.setDomainDcnDetails(new DomainDcnDetails());
        domain.getDomainDcnDetails().setDcnConfigured(false);
        domain.getDomainDcnDetails().setDomainCodename(DEF_DOM_NAME);
        domain.getDomainDcnDetails().setDcnDeploymentType(DcnDeploymentType.NONE);
        domain.setDomainTechDetails(new DomainTechDetails());
        domain.getDomainTechDetails().setDomainCodename(DEF_DOM_NAME);
        return domain;
    }

    private DomainRequest getDefaultDomainRequest(String name) {
        DomainRequest domain = new DomainRequest(name, name, true);
        domain.getDomainDcnDetails().setDcnDeploymentType(DcnDeploymentType.NONE);
        domain.getDomainDcnDetails().setDcnConfigured(false);
        return domain;
    }
}

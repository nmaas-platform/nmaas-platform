package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.CustomerNetwork;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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

    @SpyBean
    private UserService userService;

    @MockBean
    private ApplicationStatePerDomainService applicationStatePerDomainService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ModelMapper modelMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup(){
        mvc = createMVC();
        when(domainService.getDomains()).thenReturn(Arrays.asList(getDefaultDomain(), getGlobalDomain()));
        when(domainService.findDomain(DEF_DOM_ID)).thenReturn(Optional.of(getDefaultDomain()));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(getGlobalDomain()));
    }

    @Test
    public void shouldCreateDomain() throws Exception {
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
    public void shouldNotCreateDomainWhenNameIsTaken() throws Exception {
        when(domainService.existsDomain("GLOBAL")).thenReturn(true);
        DomainRequest domainRequest = getDefaultDomainRequest("test");
        domainRequest.setCodename("GLOBAL");
        domainRequest.setName("GLOBAL");
        mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(domainRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateDomain() throws Exception {
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
        verify(domainService, times(1)).updateDomain(any());
    }

    @Test
    public void shouldNotUpdateDomainWhenIdIsIncorrect() throws Exception {
        DomainView request = modelMapper.map(getDefaultDomainRequest("test"), DomainView.class);
        request.setId(999L);
        mvc.perform(put("/api/domains/" + DEF_DOM_ID)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateWithExternalServiceDomainSpecified() throws Exception {
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
    public void shouldUpdateDomainTechDetails() throws Exception {
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
    public void shouldNotUpdateDomainTechDetailsAsDomainAdmin() throws Exception {
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
    public void shouldNotUpdateTechDetailsWithCorruptedId() throws Exception {
        Domain domain = getDefaultDomain();
        domain.setApplicationStatePerDomain(new ArrayList<>());
        DomainView request = modelMapper.map(domain, DomainView.class);
        long id = request.getId();
        request.setId(123L);
        mvc.perform(patch("/api/domains/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldChangeDomainState() throws Exception {
        mvc.perform(patch("/api/domains/" + DEF_DOM_ID + "/state?active=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldEnableDcnConfiguredFlag() throws Exception {
        when(domainService.changeDcnConfiguredFlag(DEF_DOM_ID, true)).thenReturn(getDefaultDomain());
        MvcResult result = mvc.perform(patch("/api/domains/" + DEF_DOM_ID + "/dcn?configured=true")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(domainService, times(1)).changeDcnConfiguredFlag(DEF_DOM_ID, true);
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDisableDcnConfiguredFlag() throws Exception {
        when(domainService.changeDcnConfiguredFlag(DEF_DOM_ID, false)).thenReturn(getDefaultDomain());
        MvcResult result = mvc.perform(patch("/api/domains/" + DEF_DOM_ID + "/dcn?configured=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(domainService, times(1)).changeDcnConfiguredFlag(DEF_DOM_ID, false);
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDeleteDomain() throws Exception {
        when(domainService.removeDomain(DEF_DOM_ID)).thenReturn(true);
        mvc.perform(delete("/api/domains/" + DEF_DOM_ID)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotDeleteDomain() throws Exception {
        mvc.perform(delete("/api/domains/" + 1234)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetDomain() throws Exception {
        MvcResult result = mvc.perform(get("/api/domains/{domainId}", DEF_DOM_ID)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView domain = objectMapper.readValue(result.getResponse().getContentAsString(), DomainView.class);
        assertEquals(DEF_DOM_NAME, domain.getName());
    }

    @Test
    public void shouldNotGetDomain() throws Exception {
        when(domainService.findDomain(2345L)).thenReturn(Optional.empty());
        mvc.perform(get("/api/domains/{domainId}", 2345)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldGetDomains() throws Exception {
        MvcResult result = mvc.perform(get("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains("GLOBAL"));
    }

    @Test
    public void shouldGetMyDomains() throws Exception {
        when(userService.findByUsername(any())).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(domainService.getUserDomains(1L)).thenReturn(Sets.newSet(getDefaultDomain(), getGlobalDomain()));
        MvcResult result = mvc.perform(get("/api/domains/my")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains("GLOBAL"));
    }

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

    private DomainRequest getDefaultDomainRequest(String name){
        DomainRequest domain = new DomainRequest(name, name, true);
        domain.getDomainDcnDetails().setDcnDeploymentType(DcnDeploymentType.NONE);
        domain.getDomainDcnDetails().setDcnConfigured(false);
        return domain;
    }
}

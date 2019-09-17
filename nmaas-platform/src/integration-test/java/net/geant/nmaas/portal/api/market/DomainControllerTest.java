package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.util.Collections;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.CustomerNetwork;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class DomainControllerTest extends BaseControllerTestSetup {

    private static final String DEF_DOM_NAME = "defdom";

    @Autowired
    private DomainRepository domainRepo;

    @Autowired
    private DcnInfoRepository dcnInfoRepo;

    @Autowired
    private ModelMapper modelMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup(){
        mvc = createMVC();
        domainRepo.save(getDefaultDomain());
        dcnInfoRepo.save(new DcnInfo(new DcnSpec(DEF_DOM_NAME, DEF_DOM_NAME, DcnDeploymentType.NONE)));
    }

    private Domain getDefaultDomain(){
        Domain domain = new Domain(DEF_DOM_NAME, DEF_DOM_NAME, true);
        domain.setDomainDcnDetails(new DomainDcnDetails());
        domain.getDomainDcnDetails().setDcnConfigured(false);
        domain.getDomainDcnDetails().setDomainCodename(DEF_DOM_NAME);
        domain.getDomainDcnDetails().setDcnDeploymentType(DcnDeploymentType.NONE);
        domain.setDomainTechDetails(new DomainTechDetails());
        domain.getDomainTechDetails().setDomainCodename(DEF_DOM_NAME);
        return domain;
    }

    @AfterEach
    public void teardown(){
        domainRepo.findAll().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domainRepo.delete(domain));
        dcnInfoRepo.findAll().stream()
                .filter(dcnInfo -> !dcnInfo.getDomain().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(dcnInfo -> dcnInfoRepo.delete(dcnInfo));
    }

    @Test
    public void shouldCreateDomain() throws Exception {
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
    public void shouldCreateDomainWithDcnConfigured() throws Exception {
        DomainRequest domainRequest = getDefaultDomainRequest("testdcn");
        domainRequest.getDomainDcnDetails().setDcnConfigured(true);
        MvcResult result = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(domainRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldUpdateDomain() throws Exception {
        Domain request = domainRepo.findByName(DEF_DOM_NAME).get();
        request.getDomainTechDetails().setKubernetesNamespace("namespace");
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
        Domain request = domainRepo.findByName(DEF_DOM_NAME).get();
        request.getDomainDcnDetails().setCustomerNetworks(Collections.singletonList(new CustomerNetwork(null, InetAddress.getByName("1.1.1.1"), 24)));
        mvc.perform(put("/api/domains/" + request.getId())
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modelMapper.map(request, DomainView.class)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        request = domainRepo.findByName(DEF_DOM_NAME).get();
        assertFalse(request.getDomainDcnDetails().getCustomerNetworks().isEmpty());
    }

    @Test
    public void shouldNotUpdateDomainWhenIdIsIncorrect() throws Exception {
        long id = domainRepo.findByName(DEF_DOM_NAME).get().getId();
        DomainView request = modelMapper.map(getDefaultDomainRequest("test"), DomainView.class);
        request.setId(999L);
        mvc.perform(put("/api/domains/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateWithExternalServiceDomainSpecified() throws Exception {
        Domain request = domainRepo.findByName(DEF_DOM_NAME).get();
        request.getDomainTechDetails().setExternalServiceDomain("external-domain");
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
        Domain request = domainRepo.findByName(DEF_DOM_NAME).get();
        request.getDomainTechDetails().setKubernetesNamespace("namespace");
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
    public void shouldNotUpdateTechDetailsWithCorruptedId() throws Exception {
        DomainView request = modelMapper.map(domainRepo.findByName(DEF_DOM_NAME).get(), DomainView.class);
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
        long id = domainRepo.findByName(DEF_DOM_NAME).get().getId();
        mvc.perform(patch("/api/domains/" + id + "/state?active=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldEnableDcnConfiguredFlag() throws Exception {
        long id = domainRepo.findByName(DEF_DOM_NAME).get().getId();
        MvcResult result = mvc.perform(patch("/api/domains/" + id + "/dcn?configured=true")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDisableDcnConfiguredFlag() throws Exception {
        long id = domainRepo.findByName(DEF_DOM_NAME).get().getId();
        MvcResult result = mvc.perform(patch("/api/domains/" + id + "/dcn?configured=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDeleteDomain() throws Exception {
        long id = domainRepo.findByName(DEF_DOM_NAME).get().getId();
        mvc.perform(delete("/api/domains/" + id)
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
        long id = domainRepo.findByName(DEF_DOM_NAME).get().getId();
        MvcResult result = mvc.perform(get("/api/domains/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView domain = objectMapper.readValue(result.getResponse().getContentAsString(), DomainView.class);
        assertEquals(DEF_DOM_NAME, domain.getName());
    }

    @Test
    public void shouldNotGetDomain() throws Exception {
        mvc.perform(get("/api/domains/" + 2345)
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
        MvcResult result = mvc.perform(get("/api/domains/my")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains("GLOBAL"));
    }

    private DomainRequest getDefaultDomainRequest(String name){
        DomainRequest domain = new DomainRequest(name, name, true);
        domain.getDomainDcnDetails().setDcnDeploymentType(DcnDeploymentType.NONE);
        domain.getDomainDcnDetails().setDcnConfigured(false);
        return domain;
    }
}

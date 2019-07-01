package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }

    @AfterEach
    public void teardown(){
        dcnInfoRepo.deleteAll();
        domainRepo.findAll().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domainRepo.delete(domain));
    }

    @Test
    public void shouldCreateDomain() throws Exception {
        MvcResult result = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldNotCreateDomainWhenNameIsTaken() throws Exception {
        DomainRequest domainRequest = getDefaultDomain();
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
        DomainRequest domainRequest = getDefaultDomain();
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
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        long id = new JSONObject(dom.getResponse().getContentAsString()).getInt("id");
        DomainView request = modelMapper.map(getDefaultDomain(), DomainView.class);
        request.setId(id);
        MvcResult result = mvc.perform(put("/api/domains/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldNotUpdateDomainWhenIdIsIncorrect() throws Exception {
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView request = modelMapper.map(getDefaultDomain(), DomainView.class);
        request.setId(999L);
        mvc.perform(put("/api/domains/" + new JSONObject(dom.getResponse().getContentAsString()).getInt("id"))
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateWithExternalServiceDomainSpecified() throws Exception {
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView request = modelMapper.map(getDefaultDomain(), DomainView.class);
        request.getDomainTechDetails().setExternalServiceDomain("external-domain");
        request.setId((long) new JSONObject(dom.getResponse().getContentAsString()).getInt("id"));
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
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView request = modelMapper.map(getDefaultDomain(), DomainView.class);
        request.setId((long) new JSONObject(dom.getResponse().getContentAsString()).getInt("id"));
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
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView request = modelMapper.map(getDefaultDomain(), DomainView.class);
        request.setId(123L);
        mvc.perform(patch("/api/domains/" +new JSONObject(dom.getResponse().getContentAsString()).getInt("id"))
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldChangeDomainState() throws Exception {
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        mvc.perform(patch("/api/domains/" + new JSONObject(dom.getResponse().getContentAsString()).getInt("id") + "/state?active=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldEnableDcnConfiguredFlag() throws Exception {
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result = mvc.perform(patch("/api/domains/" + new JSONObject(dom.getResponse().getContentAsString()).getInt("id") + "/dcn?configured=true")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDisableDcnConfiguredFlag() throws Exception {
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result = mvc.perform(patch("/api/domains/" + new JSONObject(dom.getResponse().getContentAsString()).getInt("id") + "/dcn?configured=false")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDeleteDomain() throws Exception {
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        mvc.perform(delete("/api/domains/" + new JSONObject(dom.getResponse().getContentAsString()).getInt("id"))
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
        MvcResult dom = mvc.perform(post("/api/domains")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultDomain()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result = mvc.perform(get("/api/domains/" + new JSONObject(dom.getResponse().getContentAsString()).getInt("id"))
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        DomainView domain = objectMapper.readValue(result.getResponse().getContentAsString(), DomainView.class);
        assertEquals(getDefaultDomain().getName(), domain.getName());
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

    private DomainRequest getDefaultDomain(){
        DomainRequest domain = new DomainRequest("test", "test", true);
        domain.getDomainDcnDetails().setDcnDeploymentType(DcnDeploymentType.NONE);
        return domain;
    }
}

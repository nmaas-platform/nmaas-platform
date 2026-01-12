package net.geant.nmaas.portal.api.domains;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ResourcesLimitControllerIntTest extends BaseControllerTestSetup {

    private static final Long TEST_DOMAIN_ID = 15L;
    private static final String TEST_DOMAIN_NAME = "defdom";

    @MockitoBean
    private DomainService domainService;

    @MockitoBean
    private DomainGroupService domainGroupService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ApplicationStatePerDomainService applicationStatePerDomainService;

    @MockitoBean
    private ResourcesLimitRepository resourcesLimitRepository;

    @Autowired
    private ModelMapper modelMapper;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = createMVC();
    }

    @Test
    void shouldSetGlobalLimitForFirstTime() throws Exception {
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Collections.emptyList());
        mvc.perform(post("/api/resources-limits/global")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getDefaultGlobalLimitRequest()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private ResourcesLimitDto getDefaultGlobalLimitRequest() {
        ResourcesLimitDto dto = new ResourcesLimitDto();
        dto.setLimitType(ResourcesLimitType.GLOBAL);

        return dto;
    }

    @Test
    void shouldNotGetGlobalLimitIfNotSet() throws Exception {
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Collections.emptyList());
        mvc.perform(get("/api/resources-limits/global")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetGlobalLimit() throws Exception {
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(
                List.of(new ResourcesLimit(1L, 100, 200, 5, 10))
        );
        MvcResult result = mvc.perform(get("/api/resources-limits/global")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result).isNotNull();
        ResourcesLimitDto dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ResourcesLimitDto.class);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getLimitType()).isEqualTo(ResourcesLimitType.GLOBAL);
    }

    @Test
    void shouldGetDomainLimit() throws Exception {
        when(resourcesLimitRepository.findByDomain_Id(100L)).thenReturn(
                Optional.of(new ResourcesLimit(1L, 100, 200, 5, 10,
                        ResourcesLimitType.DOMAIN, null, new Domain(100L)))
        );
        MvcResult result = mvc.perform(get("/api/resources-limits/domain/100")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result).isNotNull();
        ResourcesLimitDto dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ResourcesLimitDto.class);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getLimitType()).isEqualTo(ResourcesLimitType.DOMAIN);
    }

    @Test
    void shouldGetGroupLimit() throws Exception {
        when(resourcesLimitRepository.findByDomainGroup_Id(100L)).thenReturn(
                Optional.of(new ResourcesLimit(1L, 100, 200, 5, 10,
                        ResourcesLimitType.DOMAIN_GROUP, new DomainGroup(100L), null))
        );
        MvcResult result = mvc.perform(get("/api/resources-limits/group/100")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result).isNotNull();
        ResourcesLimitDto dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ResourcesLimitDto.class);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getLimitType()).isEqualTo(ResourcesLimitType.DOMAIN_GROUP);
    }

}
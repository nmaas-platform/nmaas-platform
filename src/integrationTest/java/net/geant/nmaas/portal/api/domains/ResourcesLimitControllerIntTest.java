package net.geant.nmaas.portal.api.domains;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ResourcesLimitControllerIntTest extends BaseControllerTestSetup {

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
        mvc.perform(post("/api/v1/resources-limits/global")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getDefaultGlobalLimitRequest()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private ResourcesLimitDto getDefaultGlobalLimitRequest() {
        return new ResourcesLimitDto(null, 200, 100, 10, 20,
                ResourcesLimitTypeDto.GLOBAL, null, null);
    }

    @Test
    void shouldNotGetGlobalLimitIfNotSet() throws Exception {
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Collections.emptyList());
        mvc.perform(get("/api/v1/resources-limits/global")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetGlobalLimit() throws Exception {
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(
                List.of(new ResourcesLimit(1L, 100, 200, 5, 10))
        );
        MvcResult result = mvc.perform(get("/api/v1/resources-limits/global")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result).isNotNull();
        ResourcesLimitDto dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ResourcesLimitDto.class);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.limitType()).isEqualTo(ResourcesLimitTypeDto.GLOBAL);
    }

    @Test
    void shouldGetDomainLimit() throws Exception {
        when(resourcesLimitRepository.findByDomain_Id(100L)).thenReturn(
                Optional.of(new ResourcesLimit(1L, 100, 200, 5, 10,
                        ResourcesLimitType.DOMAIN, null, new Domain(100L)))
        );
        MvcResult result = mvc.perform(get("/api/v1/resources-limits/domain/100")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result).isNotNull();
        ResourcesLimitDto dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ResourcesLimitDto.class);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.limitType()).isEqualTo(ResourcesLimitTypeDto.DOMAIN);
    }

    @Test
    void shouldGetGroupLimit() throws Exception {
        when(resourcesLimitRepository.findByDomainGroup_Id(100L)).thenReturn(
                Optional.of(new ResourcesLimit(1L, 100, 200, 5, 10,
                        ResourcesLimitType.DOMAIN_GROUP, new DomainGroup(100L), null))
        );
        MvcResult result = mvc.perform(get("/api/v1/resources-limits/group/100")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result).isNotNull();
        ResourcesLimitDto dto = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ResourcesLimitDto.class);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.limitType()).isEqualTo(ResourcesLimitTypeDto.DOMAIN_GROUP);
    }

    @Test
    void shouldAllowGroupManagerToCreateGroupLimit() throws Exception {
        when(resourcesLimitRepository.existsByDomainGroup_Id(100L)).thenReturn(false);
        when(domainGroupService.getDomainGroup(100L)).thenReturn(new DomainGroupDto(100L, "group", "group"));
        when(resourcesLimitRepository.save(org.mockito.ArgumentMatchers.any(ResourcesLimit.class)))
                .thenAnswer(invocation -> {
                    ResourcesLimit limit = invocation.getArgument(0);
                    limit.setId(1L);
                    return limit;
                });

        mvc.perform(post("/api/v1/resources-limits")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ROLE_GROUP_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getDefaultGroupLimitRequest()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectGroupManagerCreatingDomainLimit() throws Exception {
        mvc.perform(post("/api/v1/resources-limits")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ROLE_GROUP_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getDefaultDomainLimitRequest()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowGroupManagerToUpdateGroupLimit() throws Exception {
        ResourcesLimit groupLimit = new ResourcesLimit(1L, 100, 200, 5, 10,
                ResourcesLimitType.DOMAIN_GROUP, new DomainGroup(100L), null);
        when(resourcesLimitRepository.findById(1L)).thenReturn(Optional.of(groupLimit));

        mvc.perform(put("/api/v1/resources-limits/1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ROLE_GROUP_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResourcesLimitUpdateDto(1L, 300, 150, 15, 25)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectGroupManagerUpdatingDomainLimit() throws Exception {
        ResourcesLimit domainLimit = new ResourcesLimit(1L, 100, 200, 5, 10,
                ResourcesLimitType.DOMAIN, null, new Domain(100L));
        when(resourcesLimitRepository.findById(1L)).thenReturn(Optional.of(domainLimit));

        mvc.perform(put("/api/v1/resources-limits/1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ROLE_GROUP_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResourcesLimitUpdateDto(1L, 300, 150, 15, 25)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowGroupManagerToDeleteGroupLimit() throws Exception {
        ResourcesLimit groupLimit = new ResourcesLimit(1L, 100, 200, 5, 10,
                ResourcesLimitType.DOMAIN_GROUP, new DomainGroup(100L), null);
        when(resourcesLimitRepository.findById(1L)).thenReturn(Optional.of(groupLimit));

        mvc.perform(delete("/api/v1/resources-limits/1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ROLE_GROUP_MANAGER))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectGroupManagerDeletingDomainLimit() throws Exception {
        ResourcesLimit domainLimit = new ResourcesLimit(1L, 100, 200, 5, 10,
                ResourcesLimitType.DOMAIN, null, new Domain(100L));
        when(resourcesLimitRepository.findById(1L)).thenReturn(Optional.of(domainLimit));

        mvc.perform(delete("/api/v1/resources-limits/1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ROLE_GROUP_MANAGER))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private ResourcesLimitDto getDefaultGroupLimitRequest() {
        return new ResourcesLimitDto(null, 200, 100, 10, 20,
                ResourcesLimitTypeDto.DOMAIN_GROUP, new DomainGroupBaseDto(100L, "group", "group", 0), null);
    }

    private ResourcesLimitDto getDefaultDomainLimitRequest() {
        DomainBaseDto domain = new DomainBaseDto();
        domain.setId(100L);
        return new ResourcesLimitDto(null, 200, 100, 10, 20,
                ResourcesLimitTypeDto.DOMAIN, null, domain);
    }

}

package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.api.dto.applications.ApplicationBaseDto;
import net.geant.nmaas.portal.api.exceptions.BundleNotFoundException;
import net.geant.nmaas.portal.api.exceptions.InvalidBundleException;
import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDescriptionDto;
import net.geant.nmaas.portal.dto.BundleDto;
import net.geant.nmaas.portal.service.BundleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BundleController.class)
@AutoConfigureMockMvc(addFilters = false)
class BundleControllerWebMvcTest {

    private static final String BASE_URL = "/api/v1/bundles";

    private static final String VALID_REQUEST = """
            {
              "name": "Test Bundle",
              "codeName": "test-bundle",
              "descriptions": [
                {
                  "language": "en",
                  "briefDescription": "brief",
                  "fullDescription": "full description"
                }
              ],
              "apps": [1, 2]
            }
            """;

    private static final String INVALID_REQUEST = """
            {
              "name": "",
              "codeName": "",
              "descriptions": [],
              "apps": [1]
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BundleService bundleService;

    @Test
    void shouldCreateBundle() throws Exception {
        when(bundleService.create(any(BundleBasicDto.class))).thenReturn(bundleDto());

        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Bundle"))
                .andExpect(jsonPath("$.codeName").value("test-bundle"))
                .andExpect(jsonPath("$.descriptions[0].language").value("en"))
                .andExpect(jsonPath("$.descriptions[0].briefDescription").value("brief"))
                .andExpect(jsonPath("$.apps[*].id", containsInAnyOrder(1, 2)));

        ArgumentCaptor<BundleBasicDto> requestCaptor = ArgumentCaptor.forClass(BundleBasicDto.class);
        verify(bundleService).create(requestCaptor.capture());
        BundleBasicDto deserializedRequest = requestCaptor.getValue();
        assertThat(deserializedRequest.name()).isEqualTo("Test Bundle");
        assertThat(deserializedRequest.codeName()).isEqualTo("test-bundle");
        assertThat(deserializedRequest.apps()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(deserializedRequest.descriptions()).hasSize(1);
        assertThat(deserializedRequest.descriptions().getFirst().language()).isEqualTo("en");
        assertThat(deserializedRequest.descriptions().getFirst().briefDescription()).isEqualTo("brief");
    }

    @Test
    void shouldNotCreateBundleWhenPayloadViolatesValidationRules() throws Exception {
        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request content."));

        verifyNoInteractions(bundleService);
    }

    @Test
    void shouldNotCreateBundleWhenPayloadIsNotJson() throws Exception {
        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-a-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bundleService);
    }

    @Test
    void shouldReturnUnprocessableEntityWhenBundleIsInvalid() throws Exception {
        when(bundleService.create(any(BundleBasicDto.class)))
                .thenThrow(new InvalidBundleException("Bundle must contain at least 2 applications"));

        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Bundle must contain at least 2 applications"));
    }

    @Test
    void shouldUpdateBundle() throws Exception {
        when(bundleService.update(any(Long.class), any(BundleBasicDto.class))).thenReturn(bundleDto());

        mvc.perform(put(BASE_URL + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Bundle"))
                .andExpect(jsonPath("$.apps[*].id", containsInAnyOrder(1, 2)));

        ArgumentCaptor<BundleBasicDto> requestCaptor = ArgumentCaptor.forClass(BundleBasicDto.class);
        verify(bundleService).update(any(Long.class), requestCaptor.capture());
        assertThat(requestCaptor.getValue().codeName()).isEqualTo("test-bundle");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatedBundleDoesNotExist() throws Exception {
        when(bundleService.update(any(Long.class), any(BundleBasicDto.class)))
                .thenThrow(new BundleNotFoundException(10L));

        mvc.perform(put(BASE_URL + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Bundle with id 10 not found"));
    }

    @Test
    void shouldGetOneBundle() throws Exception {
        when(bundleService.findById(10L)).thenReturn(bundleDto());

        mvc.perform(get(BASE_URL + "/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Bundle"))
                .andExpect(jsonPath("$.codeName").value("test-bundle"))
                .andExpect(jsonPath("$.descriptions[0].language").value("en"))
                .andExpect(jsonPath("$.apps[*].id", containsInAnyOrder(1, 2)));
    }

    @Test
    void shouldGetAllBundlesWithDefaultPagination() throws Exception {
        when(bundleService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(basicDto())));

        mvc.perform(get(BASE_URL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].name").value("Test Bundle"))
                .andExpect(jsonPath("$.content[0].codeName").value("test-bundle"))
                .andExpect(jsonPath("$.content[0].apps[*]", containsInAnyOrder(1, 2)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bundleService).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void shouldSearchBundlesByName() throws Exception {
        when(bundleService.findByName(any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(basicDto())));

        mvc.perform(get(BASE_URL)
                        .param("name", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Bundle"))
                .andExpect(jsonPath("$.content[0].apps[*]", containsInAnyOrder(1, 2)));

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(bundleService).findByName(nameCaptor.capture(), any(Pageable.class));
        assertThat(nameCaptor.getValue()).isEqualTo("test");
    }

    @Test
    void shouldGetBundlesByApplication() throws Exception {
        when(bundleService.findByApplication(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(basicDto())));

        mvc.perform(get(BASE_URL + "/application/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Bundle"))
                .andExpect(jsonPath("$.content[0].apps[*]", containsInAnyOrder(1, 2)));

        verify(bundleService).findByApplication(any(Long.class), any(Pageable.class));
    }

    @Test
    void shouldGetRawBundlesByApplication() throws Exception {
        when(bundleService.findByApplication(1L)).thenReturn(List.of(bundleDto()));

        mvc.perform(get(BASE_URL + "/application/1/raw")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Test Bundle"))
                .andExpect(jsonPath("$[0].apps[*].id", containsInAnyOrder(1, 2)));

        verify(bundleService).findByApplication(1L);
    }

    @Test
    void shouldCheckIfApplicationIsInBundle() throws Exception {
        when(bundleService.isApplicationInBundle(1L)).thenReturn(true);

        mvc.perform(get(BASE_URL + "/exists/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(bundleService).isApplicationInBundle(1L);
    }

    /**
     * MainConfig enables caching but the WebMvcTest slice does not include cache
     * auto-configuration, so a CacheManager has to be provided explicitly.
     */
    @TestConfiguration
    static class CachingTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

    }

    private BundleDto bundleDto() {
        return new BundleDto(
                10L,
                "Test Bundle",
                "test-bundle",
                List.of(new BundleDescriptionDto(100L, "en", "brief", "full description")),
                Set.of(
                        ApplicationBaseDto.builder().id(1L).name("app-1").build(),
                        ApplicationBaseDto.builder().id(2L).name("app-2").build()
                )
        );
    }

    private BundleBasicDto basicDto() {
        return new BundleBasicDto(
                10L,
                "Test Bundle",
                "test-bundle",
                List.of(new BundleDescriptionDto(100L, "en", "brief", "full description")),
                Set.of(1L, 2L)
        );
    }

}

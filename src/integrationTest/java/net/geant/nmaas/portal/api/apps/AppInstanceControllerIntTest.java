package net.geant.nmaas.portal.api.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import net.geant.nmaas.api.dto.applications.AppInstanceRequest;
import net.geant.nmaas.api.dto.domains.DomainRequest;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmCommandExecutor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@Rollback
class AppInstanceControllerIntTest extends BaseControllerTestSetup {

    private static final Domain DOMAIN = UsersHelper.DOMAIN1;

    @Autowired
    private DomainService domainService;

    @Autowired
    private ApplicationService applicationService;

    @MockitoBean
    private AppLifecycleManager appLifecycleManager;

    @MockitoBean
    private AppDeploymentMonitor appDeploymentMonitor;

    @MockitoBean
    private HelmCommandExecutor helmCommandExecutor;

    @MockitoBean
    private ApplicationStatePerDomainService applicationStatePerDomainService;

    @MockitoBean
    private ApplicationSubscriptionService applicationSubscriptionService;

    @BeforeEach
    void setup() {
        this.mvc = this.createMVC();
        when(applicationStatePerDomainService.isApplicationEnabledInDomain(any(), any(Application.class))).thenReturn(Boolean.TRUE);
        when(applicationSubscriptionService.isActive(any(), any(Domain.class))).thenReturn(Boolean.TRUE);
    }

    @Test
    void shouldDeployApplicationInstance() throws Exception {
        Application application = new Application("name", "version");
        application.setAppDeploymentSpec(AppDeploymentSpec.builder()
                .accessMethods(Set.of(AppAccessMethod.builder().build()))
                .storageVolumes(Collections.emptySet())
                .kubernetesTemplate(new KubernetesTemplate(new KubernetesChart("app", "1.0.0"), null, null))
                .build());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        Application applicationInDb = applicationService.create(application);
        Domain domainInDb = domainService.createDomain(new DomainRequest("Domain1", "domain1", true));

        AppInstanceRequest appInstanceRequest = new AppInstanceRequest(applicationInDb.getId(), "instance", true);
        mvc.perform(post("/api/apps/instances/domain/{domainId}", domainInDb.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceRequest))
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andDo(print())
                .andExpect(status().isOk());
        ArgumentCaptor<AppDeployment> appDeployment = ArgumentCaptor.forClass(AppDeployment.class);
        verify(appLifecycleManager, times(1)).deployApplication(appDeployment.capture(), any(String.class));
    }

    @Test
    void shouldForbidApplicationInstanceName() throws Exception {
        Application application = new Application("name1", "version1");
        application.setAppDeploymentSpec(AppDeploymentSpec.builder()
                .accessMethods(Set.of(AppAccessMethod.builder().build()))
                .storageVolumes(Collections.emptySet())
                .kubernetesTemplate(new KubernetesTemplate(new KubernetesChart("app", "1.0.0"), null, null))
                .build());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        Application applicationInDb = applicationService.create(application);
        domainService.createDomain(new DomainRequest("Domain1", "domain1", true));

        AppInstanceRequest appInstanceRequest = new AppInstanceRequest(applicationInDb.getId(), "instanceTooLong", true);
        MvcResult result = mvc.perform(post("/api/apps/instances/domain/{domainId}", DOMAIN.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceRequest))
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andDo(print())
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("Instance name is not valid");
    }

}

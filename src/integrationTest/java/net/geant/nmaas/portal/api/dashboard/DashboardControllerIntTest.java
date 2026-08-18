package net.geant.nmaas.portal.api.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.dashboard.DashboardDto;
import net.geant.nmaas.api.dto.dashboard.DomainDashboardDto;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserLoginRegisterRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class DashboardControllerIntTest extends BaseControllerTestSetup {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationBaseRepository applicationBaseRepository;

    @Autowired
    private AppInstanceRepository appInstanceRepository;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    @Autowired
    private UserLoginRegisterRepository userLoginRegisterRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setup() {
        mvc = createMVC();
        prepareSecurity();
    }

    @Test
    void shouldGetAdminDashboardAsSystemAdmin() throws Exception {
        String suffix = uniqueSuffix();
        Domain domain = domainRepository.saveAndFlush(new Domain("dashboard-admin-" + suffix, "dashboard-admin-" + suffix, true));
        User user = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        Application application = applicationRepository.saveAndFlush(new Application("dashboard-admin-app-" + suffix, "1.0.0"));
        applicationBaseRepository.saveAndFlush(createApplicationBase(application.getName()));

        AppInstance appInstance = new AppInstance(application, "dashboard-instance-" + suffix, domain, user, true);
        appInstance.setCreatedAt(OffsetDateTime.parse("2026-04-09T10:00:00Z").toEpochSecond() * 1000);
        appInstance.setInternalId(Identifier.newInstance("deployment-" + suffix));
        appInstance = appInstanceRepository.saveAndFlush(appInstance);
        Long appInstanceId = appInstance.getId();

        appDeploymentRepository.saveAndFlush(AppDeployment.builder()
                .deploymentId(appInstance.getInternalId())
                .domain(domain.getCodename())
                .applicationId(Identifier.newInstance(application.getId()))
                .deploymentName(appInstance.getName())
                .state(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED)
                .configFileRepositoryRequired(false)
                .configUpdateEnabled(false)
                .termsAcceptanceRequired(false)
                .owner(user.getUsername())
                .appName(application.getName())
                .instanceId(appInstance.getId())
                .descriptiveDeploymentId(Identifier.newInstance("descriptive-" + suffix))
                .build());

        OffsetDateTime startDate = OffsetDateTime.parse("2026-04-09T09:00:00Z");
        OffsetDateTime endDate = OffsetDateTime.parse("2026-04-09T11:00:00Z");

        MvcResult result = mvc.perform(get("/api/v1/dashboard/admin")
                        .param("startDate", startDate.toString())
                        .param("end", endDate.toString())
                        .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                .andExpect(status().isOk())
                .andReturn();

        DashboardDto response = objectMapper.readValue(result.getResponse().getContentAsString(), DashboardDto.class);
        assertEquals(domainRepository.count(), response.getDomainsCount());
        assertEquals(userRepository.count(), response.getUserCount());
        assertEquals(appInstanceRepository.count(), response.getInstanceCount());
        assertEquals(appInstanceRepository.countAllDeployedInTimePeriod(toMillis(startDate), toMillis(endDate)), response.getInstanceCountInPeriod());
        assertEquals(appInstanceRepository.countByName(application.getName()), response.getPopularApps().get(application.getName()));
    }

    @Test
    void shouldRejectAdminDashboardForOperator() throws Exception {
        OffsetDateTime startDate = OffsetDateTime.parse("2026-04-01T00:00:00Z");
        OffsetDateTime endDate = OffsetDateTime.parse("2026-04-07T00:00:00Z");

        mvc.perform(get("/api/v1/dashboard/admin")
                        .param("startDate", startDate.toString())
                        .param("end", endDate.toString())
                        .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_OPERATOR)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenAdminDatesAreInvalid() throws Exception {
        OffsetDateTime startDate = OffsetDateTime.parse("2026-04-07T00:00:00Z");
        OffsetDateTime endDate = OffsetDateTime.parse("2026-04-01T00:00:00Z");

        MvcResult result = mvc.perform(get("/api/v1/dashboard/admin")
                        .param("startDate", startDate.toString())
                        .param("end", endDate.toString())
                        .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("Start date is after end date."));
    }

    @Test
    void shouldGetOperatorDashboardAsOperator() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/dashboard/operator")
                        .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_OPERATOR)))
                .andExpect(status().isOk())
                .andReturn();

        DashboardDto response = objectMapper.readValue(result.getResponse().getContentAsString(), DashboardDto.class);
        assertEquals(domainRepository.countByActiveTrueAndDeletedFalse(), response.getDomainsCount());
    }

    @Test
    void shouldRejectOperatorDashboardForSystemAdmin() throws Exception {
        mvc.perform(get("/api/v1/dashboard/operator")
                        .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetDomainDashboard() throws Exception {
        String suffix = uniqueSuffix();
        Domain domain = domainRepository.saveAndFlush(new Domain("dashboard-domain-" + suffix, "dashboard-domain-" + suffix, true));
        User userWithInstance = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        userRoleRepository.saveAndFlush(new UserRole(userWithInstance, domain, Role.ROLE_DOMAIN_ADMIN));
        User userWithoutInstance = userRepository.saveAndFlush(createUser("dashboard-second-member-" + suffix, domain));
        userRoleRepository.saveAndFlush(new UserRole(userWithoutInstance, domain, Role.ROLE_DOMAIN_ADMIN));
        User userWithTwoInstances = userRepository.saveAndFlush(createUser("dashboard-third-member-" + suffix, domain));
        userRoleRepository.saveAndFlush(new UserRole(userWithTwoInstances, domain, Role.ROLE_DOMAIN_ADMIN));

        Application application = applicationRepository.saveAndFlush(new Application("dashboard-domain-app-" + suffix, "1.0.0"));

        persistInstanceForOwner(application, domain, userWithTwoInstances,
                "dashboard-domain-instance-2-" + suffix,
                Identifier.newInstance("domain-deployment-2-" + suffix),
                OffsetDateTime.parse("2026-04-09T10:32:00Z"));
        persistInstanceForOwner(application, domain, userWithInstance,
                "dashboard-domain-instance-" + suffix,
                Identifier.newInstance("domain-deployment-" + suffix),
                OffsetDateTime.parse("2026-04-09T10:30:00Z"));
        persistInstanceForOwner(application, domain, userWithTwoInstances,
                "dashboard-domain-instance-3-" + suffix,
                Identifier.newInstance("domain-deployment-3-" + suffix),
                OffsetDateTime.parse("2026-04-09T10:35:00Z"));

        OffsetDateTime loginDate = OffsetDateTime.parse("2026-04-09T08:15:30Z");
        userLoginRegisterRepository.saveAndFlush(new UserLoginRegister(loginDate, userWithInstance, UserLoginRegisterType.SUCCESS, "127.0.0.1", "localhost", "JUnit"));

        MvcResult result = mvc.perform(get("/api/v1/dashboard/domain/{id}", domain.getId())
                        .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
        DomainDashboardDto response = objectMapper.readValue(result.getResponse().getContentAsString(), DomainDashboardDto.class);

        assertEquals(loginDate, response.getUserLogins().getFirst().getLastLogin());
        assertEquals(3, response.getApplicationDeployed().size());
        assertEquals(1, response.getApplicationDeployed().stream()
                .filter(d -> d.getUserName().equals(userWithInstance.getUsername()))
                .count());
        assertEquals(1, response.getApplicationDeployed().stream()
                .filter(d -> d.getUserName().equals(userWithTwoInstances.getUsername()))
                .count());
        assertTrue(response.getApplicationUpgradeStatus().isEmpty());
    }

    private static ApplicationBase createApplicationBase(String name) {
        ApplicationBase applicationBase = new ApplicationBase(name);
        applicationBase.setOwner("integration-test");
        return applicationBase;
    }

    private void persistInstanceForOwner(Application application, Domain domain, User owner,
                                         String instanceName, Identifier internalId, OffsetDateTime createdAt) {
        AppInstance appInstance = new AppInstance(application, instanceName, domain, owner, true);
        appInstance.setCreatedAt(createdAt.toEpochSecond() * 1000);
        appInstance.setInternalId(internalId);
        appInstance = appInstanceRepository.saveAndFlush(appInstance);
        appInstance.setOwner(owner);
        appInstanceRepository.saveAndFlush(appInstance);
    }

    private static User createUser(String username, Domain domain) {
        User user = new User(username, true, "pass", domain, Role.ROLE_USER);
        user.setEmail(username + "@example.test");
        return user;
    }

    private static long toMillis(OffsetDateTime dateTime) {
        return dateTime.toEpochSecond() * 1000;
    }

    private static String uniqueSuffix() {
        return String.valueOf(System.nanoTime());
    }
}

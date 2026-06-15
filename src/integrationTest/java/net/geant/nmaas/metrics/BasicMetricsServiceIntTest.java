package net.geant.nmaas.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.geant.nmaas.kubernetes.DummyKubernetesApiClientServiceConfig;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationSubscriptionRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "nmaas.metrics.enabled=true")
@Import(DummyKubernetesApiClientServiceConfig.class)
class BasicMetricsServiceIntTest extends BaseControllerTestSetup {

    @Autowired
    private BasicUserMetricsService basicUserMetricsService;

    @Autowired
    private BasicApplicationMetricsService basicApplicationMetricsService;

    @Autowired
    private MeterRegistry meterRegistry;

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
    private ApplicationSubscriptionRepository applicationSubscriptionRepository;

    @BeforeEach
    void setup() {
        mvc = createMVC();
        prepareSecurity();
    }

    @Test
    void shouldExposeBasicUserMetricsFromRepositories() {
        String suffix = uniqueSuffix();
        Domain domain = domainRepository.saveAndFlush(new Domain("metrics-user-domain-" + suffix, "metrics-user-" + suffix, true));
        saveUser("metrics-user-domain-owner-" + suffix, domain);
        saveUserWithoutDomain("metrics-user-without-domain-" + suffix);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        basicUserMetricsService.registerMetric(registry);

        assertGaugeValue(registry, "nmaas_users_count", userRepository.count());
        assertGaugeValue(registry, "nmaas_user_no_domain_count", userRepository.count() - userRepository.countWithDomain());
        assertGaugeValue(registry, "nmaas_domains_count", domainRepository.count() - 1);
    }

    @Test
    void shouldExposeBasicApplicationMetricsFromRepositories() {
        String suffix = uniqueSuffix();
        Domain domain = domainRepository.saveAndFlush(new Domain("metrics-app-domain-" + suffix, "metrics-app-" + suffix, true));
        User owner = saveUser("metrics-app-owner-" + suffix, domain);

        Application runningApplication = saveActiveApplication("metrics-running-app-" + suffix);
        ApplicationBase runningApplicationBase = saveApplicationBase(runningApplication.getName());
        persistInstance(runningApplication, domain, owner, "metrics-running-instance-" + suffix,
                AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);

        Application removedApplication = saveActiveApplication("metrics-removed-app-" + suffix);
        saveApplicationBase(removedApplication.getName());
        persistInstance(removedApplication, domain, owner, "metrics-removed-instance-" + suffix,
                AppDeploymentState.APPLICATION_REMOVED);

        applicationSubscriptionRepository.saveAndFlush(new ApplicationSubscription(domain, runningApplicationBase, true));

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        basicApplicationMetricsService.registerMetric(registry);

        assertGaugeValue(registry, "nmaas_applications_count", applicationBaseRepository.countAllActive());
        assertGaugeValue(registry, "nmaas_application_instances_all_count", appInstanceRepository.countAllRunning());
        assertGaugeValue(registry, "nmaas_application_subscriptions_count", applicationSubscriptionRepository.count());

        Gauge runningGauge = registry.get("nmaas_application_instances_count")
                .tag("app", runningApplication.getName())
                .gauge();
        assertThat(runningGauge.value()).isEqualTo(appInstanceRepository.countRunningByName(runningApplication.getName()));

        Gauge removedGauge = registry.get("nmaas_application_instances_count")
                .tag("app", removedApplication.getName())
                .gauge();
        assertThat(removedGauge.value()).isZero();
    }

    @Test
    void shouldExposeBasicMetricsOnPrometheusActuatorEndpoint() throws Exception {
        String suffix = uniqueSuffix();
        Domain domain = domainRepository.saveAndFlush(new Domain("metrics-actuator-domain-" + suffix, "metrics-act-" + suffix, true));
        User owner = saveUser("metrics-actuator-owner-" + suffix, domain);
        saveUserWithoutDomain("metrics-actuator-no-domain-" + suffix);

        Application application = saveActiveApplication("metrics-actuator-app-" + suffix);
        ApplicationBase applicationBase = saveApplicationBase(application.getName());
        persistInstance(application, domain, owner, "metrics-actuator-instance-" + suffix,
                AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        applicationSubscriptionRepository.saveAndFlush(new ApplicationSubscription(domain, applicationBase, true));

        basicApplicationMetricsService.registerMetric(meterRegistry);

        String scrape = mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertPrometheusGauge(scrape, "nmaas_users_count", userRepository.count());
        assertPrometheusGauge(scrape, "nmaas_user_no_domain_count", userRepository.count() - userRepository.countWithDomain());
        assertPrometheusGauge(scrape, "nmaas_domains_count", domainRepository.count() - 1);
        assertPrometheusGauge(scrape, "nmaas_applications_count", applicationBaseRepository.countAllActive());
        assertPrometheusGauge(scrape, "nmaas_application_instances_all_count", appInstanceRepository.countAllRunning());
        assertPrometheusGauge(scrape, "nmaas_application_subscriptions_count", applicationSubscriptionRepository.count());
        assertPrometheusTaggedGauge(scrape, "nmaas_application_instances_count", "app", application.getName(),
                appInstanceRepository.countRunningByName(application.getName()));
    }

    private User saveUser(String username, Domain domain) {
        User user = new User(username, true, "password", domain, Role.ROLE_USER);
        user.setEmail(username + "@example.test");
        return userRepository.saveAndFlush(user);
    }

    private User saveUserWithoutDomain(String username) {
        User user = new User(username, true);
        user.setPassword("password");
        user.setEmail(username + "@example.test");
        return userRepository.saveAndFlush(user);
    }

    private Application saveActiveApplication(String name) {
        Application application = new Application(name, "1.0.0");
        application.setState(ApplicationState.ACTIVE);
        return applicationRepository.saveAndFlush(application);
    }

    private ApplicationBase saveApplicationBase(String name) {
        ApplicationBase applicationBase = new ApplicationBase(name);
        applicationBase.setOwner("integration-test");
        return applicationBaseRepository.saveAndFlush(applicationBase);
    }

    private void persistInstance(Application application, Domain domain, User owner, String instanceName, AppDeploymentState state) {
        Identifier deploymentId = Identifier.newInstance(instanceName + "-deployment");
        AppInstance appInstance = new AppInstance(application, instanceName, domain, owner, true);
        appInstance.setCreatedAt(OffsetDateTime.parse("2026-04-09T10:00:00Z").toEpochSecond() * 1000);
        appInstance.setInternalId(deploymentId);
        appInstance = appInstanceRepository.saveAndFlush(appInstance);

        appDeploymentRepository.saveAndFlush(AppDeployment.builder()
                .deploymentId(deploymentId)
                .domain(domain.getCodename())
                .applicationId(Identifier.newInstance(application.getId()))
                .deploymentName(instanceName)
                .state(state)
                .configFileRepositoryRequired(false)
                .configUpdateEnabled(false)
                .termsAcceptanceRequired(false)
                .owner(owner.getUsername())
                .appName(application.getName())
                .instanceId(appInstance.getId())
                .descriptiveDeploymentId(Identifier.newInstance(instanceName + "-descriptive"))
                .build());
    }

    private static void assertGaugeValue(MeterRegistry registry, String metricName, Number expectedValue) {
        Gauge gauge = registry.get(metricName).gauge();
        assertThat(gauge.value()).isEqualTo(expectedValue.doubleValue());
    }

    private static void assertPrometheusGauge(String scrape, String metricName, Number expectedValue) {
        assertThat(scrape).containsPattern(prometheusLine(metricName, expectedValue));
    }

    private static void assertPrometheusTaggedGauge(String scrape, String metricName, String tagName, String tagValue, Number expectedValue) {
        String escapedMetricName = Pattern.quote(metricName);
        String escapedTagName = Pattern.quote(tagName);
        String escapedTagValue = Pattern.quote(tagValue);
        String expected = Pattern.quote(Double.toString(expectedValue.doubleValue()));
        assertThat(scrape).containsPattern("(?m)^" + escapedMetricName + "(?:_number)?\\{" + escapedTagName + "=\"" + escapedTagValue + "\"}\\s+" + expected + "$");
    }

    private static String prometheusLine(String metricName, Number expectedValue) {
        return "(?m)^" + Pattern.quote(metricName) + "(?:_number)?\\s+"
                + Pattern.quote(Double.toString(expectedValue.doubleValue())) + "$";
    }

    private static String uniqueSuffix() {
        return String.valueOf(System.nanoTime());
    }
}

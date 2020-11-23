package net.geant.nmaas.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BasicApplicationMetricsService extends BaseMetricService {

    private static final String APPLICATION_COUNT_METRIC_NAME = "nmaas_applications_count";
    private static final String APPLICATION_COUNT_METRIC_DESCRIPTION = "Total NMaaS applications";

    private static final String APPLICATION_INSTANCE_RUNNING_COUNT_METRIC_NAME = "nmaas_application_instances_running_count";
    private static final String APPLICATION_INSTANCE_RUNNING_COUNT_METRIC_DESCRIPTION = "Total NMaaS running application instances";

    private static final String APPLICATION_SUBSCRIPTIONS_COUNT_METRIC_NAME = "nmaas_application_subscriptions_count";
    private static final String APPLICATION_SUBSCRIPTIONS_COUNT_METRIC_DESCRIPTION = "Total NMaaS application subscriptions";

    private static final String BASE_UNIT_NUMBER = "number";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppInstanceRepository appInstanceRepository;

    @Autowired
    private ApplicationSubscriptionRepository applicationSubscriptionRepository;

    @Override
    public void registerMetric(MeterRegistry registry) {
        Gauge.builder(APPLICATION_COUNT_METRIC_NAME, applicationRepository, apps -> (double) apps.count())
                .description(APPLICATION_COUNT_METRIC_DESCRIPTION)
                .baseUnit(BASE_UNIT_NUMBER)
                .register(registry);
        Gauge.builder(APPLICATION_INSTANCE_RUNNING_COUNT_METRIC_NAME, appInstanceRepository, instances -> (double) instances.countAllRunning())
                .description(APPLICATION_INSTANCE_RUNNING_COUNT_METRIC_DESCRIPTION)
                .baseUnit(BASE_UNIT_NUMBER)
                .register(registry);
        Gauge.builder(APPLICATION_SUBSCRIPTIONS_COUNT_METRIC_NAME, applicationSubscriptionRepository, subscriptions -> (double) subscriptions.count())
                .description(APPLICATION_SUBSCRIPTIONS_COUNT_METRIC_DESCRIPTION)
                .baseUnit(BASE_UNIT_NUMBER)
                .register(registry);
    }

}

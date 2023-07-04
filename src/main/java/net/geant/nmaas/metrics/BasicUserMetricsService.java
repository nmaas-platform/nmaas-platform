package net.geant.nmaas.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicUserMetricsService extends BaseMetricService {

    private static final String USER_COUNT_METRIC_NAME = "nmaas_users_count";
    private static final String USER_COUNT_METRIC_DESCRIPTION = "Total NMaaS users";

    private static final String USER_WITHOUT_DOMAIN_COUNT_METRIC_NAME = "nmaas_user_no_domain_count";
    private static final String USER_WITHOUT_DOMAIN_COUNT_METRIC_DESCRIPTION = "NMaaS users without domain";

    private static final String DOMAIN_COUNT_METRIC_NAME = "nmaas_domains_count";
    private static final String DOMAIN_COUNT_METRIC_DESCRIPTION = "Total NMaaS domains";

    private static final String BASE_UNIT_NUMBER = "number";

    private final UserRepository userRepository;
    private final DomainRepository domainRepository;

    @Override
    public void registerMetric(MeterRegistry registry) {
        Gauge.builder(USER_COUNT_METRIC_NAME, userRepository, users -> (double) users.count())
                .description(USER_COUNT_METRIC_DESCRIPTION)
                .baseUnit(BASE_UNIT_NUMBER)
                .register(registry);
        Gauge.builder(USER_WITHOUT_DOMAIN_COUNT_METRIC_NAME, userRepository, users -> (double) users.count() - users.countWithDomain())
                .description(USER_WITHOUT_DOMAIN_COUNT_METRIC_DESCRIPTION)
                .baseUnit(BASE_UNIT_NUMBER)
                .register(registry);
        Gauge.builder(DOMAIN_COUNT_METRIC_NAME, domainRepository, domains -> (double) domains.count() - 1)
                .description(DOMAIN_COUNT_METRIC_DESCRIPTION)
                .baseUnit(BASE_UNIT_NUMBER)
                .register(registry);
    }

}

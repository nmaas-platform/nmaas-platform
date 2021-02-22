package net.geant.nmaas.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
abstract class BaseMetricService implements MeterBinder {

    @Value("${nmaas.metrics.enabled}")
    private boolean enabled;

    @Override
    public void bindTo(MeterRegistry registry) {
        if (enabled) {
            registerMetric(registry);
        }
    }

    abstract void registerMetric(MeterRegistry registry);

}

package net.geant.nmaas.kubernetes;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class DummyKubernetesApiClientServiceConfig {

    @Bean
    @Primary
    public DummyKubernetesApiClientService dummyKubernetesApiClientService() {
        return new DummyKubernetesApiClientService();
    }

}

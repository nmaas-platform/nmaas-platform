package net.geant.nmaas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() {
//        return Clock.systemUTC();
        return Clock.system(ZoneId.of("Europe/Warsaw"));
    }
}
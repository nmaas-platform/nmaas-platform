package net.geant.nmaas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
class JacksonConfig {

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }
}

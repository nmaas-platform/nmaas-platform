package net.geant.nmaas.orchestration.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.JacksonDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfigurationView implements Serializable {

    @JsonProperty("jsonInput")
    @JsonDeserialize(using = JacksonDeserializer.class)
    private String jsonInput;

    @JsonProperty("storageSpace")
    private Integer storageSpace;

    @JsonProperty("additionalParameters")
    @JsonDeserialize(using = JacksonDeserializer.class)
    private String additionalParameters;

    @JsonProperty("mandatoryParameters")
    @JsonDeserialize(using = JacksonDeserializer.class)
    private String mandatoryParameters;

    @JsonProperty("accessCredentials")
    @JsonDeserialize(using = JacksonDeserializer.class)
    private String accessCredentials;

    @JsonProperty("termsAcceptance")
    @JsonDeserialize(using = JacksonDeserializer.class)
    private String termsAcceptance;

}
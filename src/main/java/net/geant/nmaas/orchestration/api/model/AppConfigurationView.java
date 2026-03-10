package net.geant.nmaas.orchestration.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfigurationView implements Serializable {

    @JsonProperty("jsonInput")
    private JsonNode jsonInput;

    @JsonProperty("storageSpace")
    private Integer storageSpace;

    @JsonProperty("additionalParameters")
    private JsonNode additionalParameters;

    @JsonProperty("mandatoryParameters")
    private JsonNode mandatoryParameters;

    @JsonProperty("accessCredentials")
    private JsonNode accessCredentials;

    @JsonProperty("termsAcceptance")
    private JsonNode termsAcceptance;

}
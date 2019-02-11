package net.geant.nmaas.orchestration.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigurationView {

    @JsonProperty("jsonInput")
    private String jsonInput;

    private Integer storageSpace;

    @JsonProperty("additionalParameters")
    private String additionalParameters;

    @JsonProperty("mandatoryParameters")
    private String mandatoryParameters;
    @JsonProperty("accessCredentials")
    private String accessCredentials;

    @JsonSetter("additionalParameters")
    public void setAdditionalParameters(JsonNode data){
        this.additionalParameters = data.toString();
    }

    @JsonSetter("mandatoryParameters")
    public void setMandatoryParameters(JsonNode data) { this.mandatoryParameters = data.toString();}

    public void setStorageSpace(Integer storageSpace){
        this.storageSpace = storageSpace;
    }

    @JsonSetter("jsonInput")
    public void setJsonInput(JsonNode data){
        this.jsonInput = data.toString();
    }

    @JsonSetter("accessCredentials")
    public void setAccessCredentials(JsonNode data){
        this.accessCredentials = data.toString();
    }
}
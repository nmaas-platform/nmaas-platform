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

    @JsonSetter("additionalParameters")
    public void setAdditionalParameters(JsonNode data){
        this.additionalParameters = data.toString();
    }

    public void setStorageSpace(Integer storageSpace){
        this.storageSpace = storageSpace;
    }

    @JsonSetter("jsonInput")
    public void setJsonInput(JsonNode data){
        this.jsonInput = data.toString();
    }
}
package net.geant.nmaas.orchestration.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfigurationView implements Serializable {

    @JsonProperty("jsonInput")
    private String jsonInput;

    private Integer storageSpace;

    @JsonProperty("additionalParameters")
    private String additionalParameters;

    @JsonProperty("mandatoryParameters")
    private String mandatoryParameters;

    @JsonProperty("accessCredentials")
    private String accessCredentials;

    /**
     * NMAAS-967
     * terms acceptance config
     */
    @JsonProperty("termsAcceptance")
    private String termsAcceptance;

    @JsonSetter("additionalParameters")
    public void setAdditionalParameters(JsonNode data){
        this.additionalParameters = data.toString();
    }

    @JsonSetter("mandatoryParameters")
    public void setMandatoryParameters(JsonNode data) {
        this.mandatoryParameters = data.toString();
    }

    public void setMandatoryParameters(String data) {
        this.mandatoryParameters = data;
    }

    public void setStorageSpace(Integer storageSpace){
        this.storageSpace = storageSpace;
    }

    @JsonSetter("jsonInput")
    public void setJsonInput(JsonNode data){
        this.jsonInput = data.toString();
    }

    public void setJsonInput(String data) {
        this.jsonInput = data;
    }

    @JsonSetter("accessCredentials")
    public void setAccessCredentials(JsonNode data){
        this.accessCredentials = data.toString();
    }

    @JsonSetter("termsAcceptance")
    public void setTermsAcceptance(JsonNode data) {this.termsAcceptance = data.toString();}

    public void setTermsAcceptance(String data) {this.termsAcceptance = data; }

}
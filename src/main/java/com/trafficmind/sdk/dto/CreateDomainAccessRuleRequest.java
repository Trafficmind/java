package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateDomainAccessRuleRequest {

    @NotNull
    @JsonProperty("configuration")
    private CreateAccessRuleConfiguration configuration;

    @NotNull
    @JsonProperty("mode")
    private AccessRuleMode mode;

    @JsonProperty("notes")
    private String notes;

    public CreateDomainAccessRuleRequest() {}

    public CreateAccessRuleConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CreateAccessRuleConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getMode() {
        return mode == null ? null : mode.getValue();
    }

    public void setMode(String mode) {
        this.mode = AccessRuleMode.fromValue(mode);
    }

    public AccessRuleMode getModeEnum() {
        return mode;
    }

    public void setModeEnum(AccessRuleMode mode) {
        this.mode = mode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateDomainAccessRuleRequest{" + "configuration=" + configuration + ", " + "mode=" + mode + ", " + "notes=" + notes + "}";
    }
}

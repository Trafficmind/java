package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAccessRuleConfiguration {

    /** Configuration target. */
    @NotNull
    @JsonProperty("target")
    private AccessRuleTarget target;

    /** The value of type. */
    @NotNull
    @JsonProperty("value")
    private String value;

    public CreateAccessRuleConfiguration() {}

    public String getTarget() {
        return target == null ? null : target.getValue();
    }

    public void setTarget(String target) {
        this.target = AccessRuleTarget.fromValue(target);
    }

    public AccessRuleTarget getTargetEnum() {
        return target;
    }

    public void setTargetEnum(AccessRuleTarget target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CreateAccessRuleConfiguration{" + "target=" + target + ", " + "value=" + value + "}";
    }
}

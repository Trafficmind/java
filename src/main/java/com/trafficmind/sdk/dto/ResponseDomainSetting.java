package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDomainSetting {

    /** domain setting ID
Unique identifier for the domain setting. */
    @JsonProperty("id")
    private String id;

    /** Setting value
The current value of the domain setting. */
    @JsonProperty("value")
    private String value;

    public ResponseDomainSetting() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ResponseDomainSetting{" + "id=" + id + ", " + "value=" + value + "}";
    }
}
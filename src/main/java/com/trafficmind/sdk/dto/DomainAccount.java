package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainAccount {

    /** ID is the unique identifier for the account.
This ID is used to reference the account in various operations. */
    @JsonProperty("id")
    private String id;

    /** Name is the human-readable name of the account.
This name is used to identify the account in the user interface or in responses. */
    @JsonProperty("name")
    private String name;

    public DomainAccount() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DomainAccount{" + "id=" + id + ", " + "name=" + name + "}";
    }
}
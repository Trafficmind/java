package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDNSDelete {

    /** ID is the unique identifier of the DNS record to be deleted.
This ID is used to find and delete the specified record. */
    @JsonProperty("id")
    private String id;

    public BatchDNSDelete() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BatchDNSDelete{" + "id=" + id + "}";
    }
}
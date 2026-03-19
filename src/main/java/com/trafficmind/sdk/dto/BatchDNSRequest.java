package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDNSRequest {

    @JsonProperty("deletes")
    private List<BatchDNSDelete> deletes;

    @JsonProperty("creates")
    private List<BatchDNSCreate> creates;

    @JsonProperty("replaces")
    private List<BatchDNSReplace> replaces;

    @JsonProperty("updates")
    private List<BatchDNSUpdate> updates;

    public BatchDNSRequest() {}

    public List<BatchDNSDelete> getDeletes() {
        return deletes;
    }

    public void setDeletes(List<BatchDNSDelete> deletes) {
        this.deletes = deletes;
    }

    public List<BatchDNSCreate> getCreates() {
        return creates;
    }

    public void setCreates(List<BatchDNSCreate> creates) {
        this.creates = creates;
    }

    public List<BatchDNSReplace> getReplaces() {
        return replaces;
    }

    public void setReplaces(List<BatchDNSReplace> replaces) {
        this.replaces = replaces;
    }

    public List<BatchDNSUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<BatchDNSUpdate> updates) {
        this.updates = updates;
    }

    @Override
    public String toString() {
        return "BatchDNSRequest{" + "deletes=" + deletes + ", " + "creates=" + creates + ", " + "replaces=" + replaces + ", " + "updates=" + updates + "}";
    }
}

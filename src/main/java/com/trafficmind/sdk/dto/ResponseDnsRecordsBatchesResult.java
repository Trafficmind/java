package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDnsRecordsBatchesResult {

    /** Deletes contains the DNS records that were deleted in the batch operation.
These records are identified by their unique IDs and will no longer exist in the system. */
    @JsonProperty("deletes")
    private List<DomainDNSRecord> deletes;

    /** Creates contains the DNS records that were created in the batch operation.
These records represent new DNS records that were added to the system. */
    @JsonProperty("creates")
    private List<DomainDNSRecord> creates;

    /** Replaces contains the DNS records that were fully replaced in the batch operation.
These records are replaced with new data, typically including all fields. */
    @JsonProperty("replaces")
    private List<DomainDNSRecord> replaces;

    /** Updates contains the DNS records that were updated in the batch operation.
These records contain the modified fields that were updated during the operation. */
    @JsonProperty("updates")
    private List<DomainDNSRecord> updates;

    public ResponseDnsRecordsBatchesResult() {}

    public List<DomainDNSRecord> getDeletes() {
        return deletes;
    }

    public void setDeletes(List<DomainDNSRecord> deletes) {
        this.deletes = deletes;
    }

    public List<DomainDNSRecord> getCreates() {
        return creates;
    }

    public void setCreates(List<DomainDNSRecord> creates) {
        this.creates = creates;
    }

    public List<DomainDNSRecord> getReplaces() {
        return replaces;
    }

    public void setReplaces(List<DomainDNSRecord> replaces) {
        this.replaces = replaces;
    }

    public List<DomainDNSRecord> getUpdates() {
        return updates;
    }

    public void setUpdates(List<DomainDNSRecord> updates) {
        this.updates = updates;
    }

    @Override
    public String toString() {
        return "ResponseDnsRecordsBatchesResult{" + "deletes=" + deletes + ", " + "creates=" + creates + ", " + "replaces=" + replaces + ", " + "updates=" + updates + "}";
    }
}

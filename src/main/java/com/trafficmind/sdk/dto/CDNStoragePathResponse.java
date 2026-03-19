package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CDNStoragePathResponse {

    @JsonProperty("domain_name")
    private String domainName;

    /** computed: subdomain.domain/path_prefix */
    @JsonProperty("full_path")
    private String fullPath;

    @JsonProperty("id")
    private String id;

    @JsonProperty("path_prefix")
    private String pathPrefix;

    @JsonProperty("storage_id")
    private String storageId;

    @JsonProperty("subdomain")
    private String subdomain;

    @JsonProperty("domain_id")
    private String domainId;

    public CDNStoragePathResponse() {}

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String toString() {
        return "CDNStoragePathResponse{" + "domainName=" + domainName + ", " + "fullPath=" + fullPath + ", " + "id=" + id + ", " + "pathPrefix=" + pathPrefix + ", " + "storageId=" + storageId + ", " + "subdomain=" + subdomain + ", " + "domainId=" + domainId + "}";
    }
}

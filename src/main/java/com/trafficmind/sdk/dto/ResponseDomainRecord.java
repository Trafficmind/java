package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDomainRecord {

    /** Associated account information. */
    @JsonProperty("account")
    private DomainAccount account;

    /** Group identifier for the domain */
    @JsonProperty("group_id")
    private Integer groupId;

    /** Unique identifier for the domain. */
    @JsonProperty("id")
    private String id;

    /** Domain name of the domain. */
    @JsonProperty("name")
    private String name;

    /** Nameservers associated with the domain */
    @JsonProperty("assigned_nameservers")
    private List<String> nameServers;

    /** Original Nameservers given you by DNS provider */
    @JsonProperty("original_nameservers")
    private List<String> originalNameServers;

    public ResponseDomainRecord() {}

    public DomainAccount getAccount() {
        return account;
    }

    public void setAccount(DomainAccount account) {
        this.account = account;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

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

    public List<String> getNameServers() {
        return nameServers;
    }

    public void setNameServers(List<String> nameServers) {
        this.nameServers = nameServers;
    }

    public List<String> getOriginalNameServers() {
        return originalNameServers;
    }

    public void setOriginalNameServers(List<String> originalNameServers) {
        this.originalNameServers = originalNameServers;
    }

    @Override
    public String toString() {
        return "ResponseDomainRecord{" + "account=" + account + ", " + "groupId=" + groupId + ", " + "id=" + id + ", " + "name=" + name + ", " + "nameServers=" + nameServers + ", " + "originalNameServers=" + originalNameServers + "}";
    }
}

package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainDNSRecord {

    /** Notes about the record
A comment or note associated with the DNS record. */
    @JsonProperty("comment")
    private String comment;

    /** DNS Record content based on the record type
The content of the DNS record, varying based on the record type. */
    @JsonProperty("content")
    private String content;

    /** Record ID
Unique identifier for the DNS record. */
    @JsonProperty("id")
    private String id;

    /** Record name
The name associated with the DNS record, typically representing a domain or subdomain. */
    @JsonProperty("name")
    private String name;

    /** Whether proxied through our system
A flag indicating if the DNS record is being proxied through our system. */
    @JsonProperty("proxied")
    private Boolean proxied;

    /** Time to live
The TTL (Time To Live) in seconds for the DNS record. */
    @JsonProperty("ttl")
    private Integer ttl;

    /** Record type
The type of the DNS record (e.g., A, AAAA, CNAME, etc.). */
    @JsonProperty("type")
    private String type;

    public DomainDNSRecord() {}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Boolean getProxied() {
        return proxied;
    }

    public void setProxied(Boolean proxied) {
        this.proxied = proxied;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DomainDNSRecord{" + "comment=" + comment + ", " + "content=" + content + ", " + "id=" + id + ", " + "name=" + name + ", " + "proxied=" + proxied + ", " + "ttl=" + ttl + ", " + "type=" + type + "}";
    }
}
package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainRecordListPayload {

    @JsonProperty("items")
    private List<DomainDNSRecord> items;

    @JsonProperty("pagination")
    private ResultInfo pagination;

    @JsonProperty("search_query")
    private String searchQuery;

    public DomainRecordListPayload() {}

    public List<DomainDNSRecord> getItems() {
        return items;
    }

    public void setItems(List<DomainDNSRecord> items) {
        this.items = items;
    }

    public ResultInfo getPagination() {
        return pagination;
    }

    public void setPagination(ResultInfo pagination) {
        this.pagination = pagination;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}

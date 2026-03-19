package com.trafficmind.sdk.request;

import com.trafficmind.sdk.internal.QueryParams;

import java.util.Map;

public final class DomainListRequest {
    private DomainMatchMode matchMode;
    private Integer page;
    private Integer pageSize;
    private String query;

    public DomainListRequest() {}

    public static DomainListRequest create() {
        return new DomainListRequest();
    }

    public DomainListRequest matchMode(DomainMatchMode matchMode) {
        this.matchMode = matchMode;
        return this;
    }

    public DomainListRequest matchMode(String matchMode) {
        this.matchMode = DomainMatchMode.fromValue(matchMode);
        return this;
    }

    public DomainListRequest page(Integer page) {
        this.page = page;
        return this;
    }

    public DomainListRequest pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public DomainListRequest query(String query) {
        this.query = query;
        return this;
    }

    public String getMatchMode() {
        return matchMode == null ? null : matchMode.getValue();
    }

    public DomainMatchMode getMatchModeEnum() {
        return matchMode;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, String> toQueryParams() {
        Map<String, String> q = QueryParams.of();
        QueryParams.putIfNotNull(q, "match_mode", getMatchMode());
        QueryParams.putIfNotNull(q, "page", page);
        QueryParams.putIfNotNull(q, "page_size", pageSize);
        QueryParams.putIfNotNull(q, "query", query);
        return q;
    }
}

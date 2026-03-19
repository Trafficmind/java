package com.trafficmind.sdk.request;

import com.trafficmind.sdk.internal.QueryParams;

import java.util.Map;

public final class DnsListRequest {
    private Integer page;
    private Integer pageSize;
    private String query;

    public DnsListRequest() {}

    public static DnsListRequest create() {
        return new DnsListRequest();
    }

    public DnsListRequest page(Integer page) {
        this.page = page;
        return this;
    }

    public DnsListRequest pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public DnsListRequest query(String query) {
        this.query = query;
        return this;
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
        QueryParams.putIfNotNull(q, "page", page);
        QueryParams.putIfNotNull(q, "page_size", pageSize);
        QueryParams.putIfNotNull(q, "query", query);
        return q;
    }
}

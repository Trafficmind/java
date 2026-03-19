package com.trafficmind.sdk.request;

import com.trafficmind.sdk.internal.QueryParams;

import java.util.Map;

public final class CdnStorageListRequest {
    private Integer page;
    private Integer pageSize;

    public CdnStorageListRequest() {}

    public static CdnStorageListRequest create() {
        return new CdnStorageListRequest();
    }

    public CdnStorageListRequest page(Integer page) {
        this.page = page;
        return this;
    }

    public CdnStorageListRequest pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Map<String, String> toQueryParams() {
        Map<String, String> q = QueryParams.of();
        QueryParams.putIfNotNull(q, "page", page);
        QueryParams.putIfNotNull(q, "page_size", pageSize);
        return q;
    }
}

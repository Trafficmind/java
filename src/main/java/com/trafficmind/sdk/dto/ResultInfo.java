package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultInfo {

    /** Number of items returned in the current page. */
    @JsonProperty("items")
    private Integer items;

    /** Current page number. */
    @JsonProperty("page")
    private Integer page;

    /** Number of items requested per page (maps to API field {@code page_size}). */
    @JsonProperty("page_size")
    private Integer pageSize;

    /** Total number of items available. */
    @JsonProperty("total")
    private Integer totalCount;

    public ResultInfo() {}

    public Integer getItems() {
        return items;
    }

    public void setItems(Integer items) {
        this.items = items;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "ResultInfo{" + "items=" + items + ", " + "page=" + page + ", " + "pageSize=" + pageSize + ", " + "totalCount=" + totalCount + "}";
    }
}

package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unified SDK response wrapper that exposes payload + API envelope fields.
 *
 * @param <T> parsed payload type extracted from API "payload"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

    private final T result;
    private final ResultInfo resultInfo;
    private final ResponseMeta meta;
    private final ApiResponseStatus status;
    private final JsonNode payload;

    public ApiResponse(T result, ResultInfo resultInfo, ResponseMeta meta, ApiResponseStatus status) {
        this(result, resultInfo, meta, status, null);
    }

    public ApiResponse(T result, ResultInfo resultInfo, ResponseMeta meta, ApiResponseStatus status, JsonNode payload) {
        this.result = result;
        this.resultInfo = resultInfo;
        this.meta = meta;
        this.status = status;
        this.payload = payload;
    }

    public T getResult() {
        return result;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public ResponseMeta getMeta() {
        return meta;
    }

    public ApiResponseStatus getStatus() {
        return status;
    }

    /**
     * Returns raw payload node so callers can read endpoint-specific fields
     * that are not modeled explicitly in SDK DTOs (for example {@code search_query}).
     */
    public JsonNode getPayload() {
        return payload;
    }

    public <R> ApiResponse<R> withResult(R value) {
        return new ApiResponse<>(value, resultInfo, meta, status, payload);
    }
}

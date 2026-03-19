package com.trafficmind.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ApiResponseStatus;
import com.trafficmind.sdk.dto.ResponseMeta;
import com.trafficmind.sdk.dto.ResultInfo;

import java.io.IOException;
import java.util.List;

/**
 * Internal response parser/mapper for API envelopes.
 */
final class ResponseCodec {
    private static final String[] PAYLOAD_RESULT_KEYS = {
            "records", "items", "batch", "result", "domain", "storage", "user", "setting", "sync"
    };

    private final ObjectMapper mapper;

    ResponseCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    <T> T readResult(JsonNode root, Class<T> clazz) {
        JsonNode result = extractResultNode(root);
        try {
            return mapper.treeToValue(result, clazz);
        } catch (JsonProcessingException e) {
            throw new TrafficmindException("Failed to deserialize response: " + e.getMessage(), 0, e);
        }
    }

    <T> T readBody(JsonNode root, Class<T> clazz) {
        try {
            return mapper.treeToValue(root == null ? MissingNode.getInstance() : root, clazz);
        } catch (JsonProcessingException e) {
            throw new TrafficmindException("Failed to deserialize response: " + e.getMessage(), 0, e);
        }
    }

    JsonNode extractResultNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return MissingNode.getInstance();
        }
        JsonNode payload = root.path("payload");
        if (!payload.isObject()) {
            return MissingNode.getInstance();
        }

        for (String key : PAYLOAD_RESULT_KEYS) {
            if (payload.has(key)) {
                return payload.get(key);
            }
        }
        return payload;
    }

    JsonNode extractResultInfoNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return MissingNode.getInstance();
        }
        JsonNode payload = root.path("payload");
        if (payload.isObject() && payload.has("pagination")) {
            return payload.get("pagination");
        }
        return MissingNode.getInstance();
    }

    JsonNode extractPayloadNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return MissingNode.getInstance();
        }
        JsonNode payload = root.path("payload");
        if (payload.isObject()) {
            return payload;
        }
        return MissingNode.getInstance();
    }

    JsonNode extractResponseMetaNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return MissingNode.getInstance();
        }
        JsonNode meta = root.path("meta");
        if (meta.isObject()) {
            return meta;
        }
        return MissingNode.getInstance();
    }

    JsonNode extractResponseStatusNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return MissingNode.getInstance();
        }
        JsonNode status = root.path("status");
        if (status.isObject()) {
            return status;
        }
        return MissingNode.getInstance();
    }

    ResultInfo readResultInfo(JsonNode root) {
        JsonNode infoNode = extractResultInfoNode(root);
        if (infoNode == null || infoNode.isMissingNode()) {
            return null;
        }
        return readBody(infoNode, ResultInfo.class);
    }

    ResponseMeta readResponseMeta(JsonNode root) {
        JsonNode metaNode = extractResponseMetaNode(root);
        if (metaNode == null || metaNode.isMissingNode()) {
            return null;
        }
        return readBody(metaNode, ResponseMeta.class);
    }

    ApiResponseStatus readResponseStatus(JsonNode root) {
        JsonNode statusNode = extractResponseStatusNode(root);
        if (statusNode == null || statusNode.isMissingNode()) {
            return null;
        }
        return readBody(statusNode, ApiResponseStatus.class);
    }

    <T> ApiResponse<T> readApiResponse(JsonNode root, Class<T> clazz) {
        T result = readResult(root, clazz);
        return new ApiResponse<>(
                result,
                readResultInfo(root),
                readResponseMeta(root),
                readResponseStatus(root),
                extractPayloadNode(root)
        );
    }

    <T> ApiResponse<T> readApiPayloadResponse(JsonNode root, Class<T> clazz) {
        T payload = readBody(extractPayloadNode(root), clazz);
        return new ApiResponse<>(
                payload,
                readResultInfo(root),
                readResponseMeta(root),
                readResponseStatus(root),
                extractPayloadNode(root)
        );
    }

    <T> ApiResponse<List<T>> readApiResponseList(JsonNode root, Class<T> itemClass) {
        List<T> result = readResultList(root, itemClass);
        return new ApiResponse<>(
                result,
                readResultInfo(root),
                readResponseMeta(root),
                readResponseStatus(root),
                extractPayloadNode(root)
        );
    }

    <T> List<T> readResultList(JsonNode root, Class<T> itemClass) {
        JsonNode result = extractResultNode(root);
        try {
            CollectionType ct = mapper.getTypeFactory().constructCollectionType(List.class, itemClass);
            return mapper.readValue(mapper.treeAsTokens(result), ct);
        } catch (IOException e) {
            throw new TrafficmindException("Failed to deserialize response list: " + e.getMessage(), 0, e);
        }
    }
}

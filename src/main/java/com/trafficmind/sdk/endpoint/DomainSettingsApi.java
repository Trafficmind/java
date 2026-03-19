package com.trafficmind.sdk.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.UpdateDomainSettingRequest;
import com.trafficmind.sdk.dto.ResponseDomainSetting;

import java.util.Map;

/** Domain Settings endpoint wrapper. */
public final class DomainSettingsApi {
    private final TrafficmindClient client;

    public DomainSettingsApi(TrafficmindClient client) {
        this.client = client;
    }

    /** Fetch a single domain setting by name. */
    public ApiResponse<ResponseDomainSetting> get(String domainId, String settingId) {
        JsonNode root = client.getRaw(client.path("domains", domainId, "settings", settingId), Map.of());
        return client.readApiResponse(root, ResponseDomainSetting.class);
    }

    /** Update a single domain setting by identifier. */
    public ApiResponse<ResponseDomainSetting> update(String domainId, String settingId, UpdateDomainSettingRequest request) {
        JsonNode root = client.patchRaw(client.path("domains", domainId, "settings", settingId), request);
        return client.readApiResponse(root, ResponseDomainSetting.class);
    }
}

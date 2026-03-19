package com.trafficmind.sdk.endpoint;

import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.BasicPayload;
import com.trafficmind.sdk.dto.CreateAccountAccessRuleRequest;
import com.trafficmind.sdk.dto.CreateDomainAccessRuleRequest;
import com.fasterxml.jackson.databind.JsonNode;

/** WAF endpoints wrapper (Account WAF + Domain WAF). */
public final class WafApi {
    private final TrafficmindClient client;

    public WafApi(TrafficmindClient client) {
        this.client = client;
    }

    /**
     * Account WAF. Creates a new IP access rule for an account.
     * The rule will apply to all domains in the account.
     */
    public ApiResponse<BasicPayload> createAccountWafRule(CreateAccountAccessRuleRequest request, String accountId) {
        JsonNode root = client.postRaw(client.path("accounts", accountId, "firewall_rules"), request);
        return client.readApiPayloadResponse(root, BasicPayload.class);
    }

    /**
     * Domain WAF. Creates a new IP access rule for a domain.
     */
    public ApiResponse<BasicPayload> createDomainWafRule(CreateDomainAccessRuleRequest request, String domainId) {
        JsonNode root = client.postRaw(client.path("domains", domainId, "firewall_rules"), request);
        return client.readApiPayloadResponse(root, BasicPayload.class);
    }
}

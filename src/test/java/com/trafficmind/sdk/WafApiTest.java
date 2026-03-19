package com.trafficmind.sdk;

import com.trafficmind.sdk.dto.CreateAccessRuleConfiguration;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.BasicPayload;
import com.trafficmind.sdk.dto.CreateAccountAccessRuleRequest;
import com.trafficmind.sdk.dto.CreateDomainAccessRuleRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WafApiTest {

    @Test
    void createAccountAnddomainWafRulesSendCorrectBodies() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("POST", "/public/v1/accounts/a1/firewall_rules", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"mode\":\"block\""), "body: " + body);
                assertTrue(body.contains("\"configuration\""), "body: " + body);
                assertTrue(body.contains("\"target\":\"ip\""), "body: " + body);
                assertTrue(body.contains("\"value\":\"1.2.3.4\""), "body: " + body);
                MockServer.writeJson(ex, 201, "{\"status\":{\"code\":\"created\"},\"payload\":{\"acknowledged\":true}}");
            });

            server.when("POST", "/public/v1/domains/z1/firewall_rules", ex -> {
                String body = MockServer.readBody(ex);
                assertTrue(body.contains("\"mode\":\"block\""), "body: " + body);
                assertTrue(body.contains("\"configuration\""), "body: " + body);
                assertTrue(body.contains("\"target\":\"ip\""), "body: " + body);
                assertTrue(body.contains("\"value\":\"5.6.7.8\""), "body: " + body);
                assertFalse(body.contains("\"scope\""), "body: " + body);
                assertFalse(body.contains("\"source\""), "body: " + body);
                MockServer.writeJson(ex, 201, "{\"status\":{\"code\":\"created\"},\"payload\":{\"acknowledged\":true}}");
            });

            TrafficmindClient client = TrafficmindClient.builder().email("e").apiKey("k").baseUrl(server.baseUri()).allowInsecureTransportForTesting(true).build();

            CreateAccessRuleConfiguration cfg = new CreateAccessRuleConfiguration();
            cfg.setTarget("ip");
            cfg.setValue("1.2.3.4");

            CreateAccountAccessRuleRequest accReq = new CreateAccountAccessRuleRequest();
            accReq.setMode("block");
            accReq.setNotes("n");
            accReq.setConfiguration(cfg);

            ApiResponse<BasicPayload> accountResponse = client.waf().createAccountWafRule(accReq, "a1");
            assertNotNull(accountResponse.getResult());
            assertTrue(Boolean.TRUE.equals(accountResponse.getResult().getAcknowledged()));

            CreateAccessRuleConfiguration domainCfg = new CreateAccessRuleConfiguration();
            domainCfg.setTarget("ip");
            domainCfg.setValue("5.6.7.8");

            CreateDomainAccessRuleRequest domainReq = new CreateDomainAccessRuleRequest();
            domainReq.setMode("block");
            domainReq.setConfiguration(domainCfg);
            domainReq.setNotes("n");

            ApiResponse<BasicPayload> domainResponse = client.waf().createDomainWafRule(domainReq, "z1");
            assertNotNull(domainResponse.getResult());
            assertTrue(Boolean.TRUE.equals(domainResponse.getResult().getAcknowledged()));
        }
    }
}

package com.trafficmind.sdk.examples;

import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.TrafficmindException;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.request.DomainListRequest;

/**
 * Minimal runnable example.
 *
 * Env vars:
 *  - TRAFFICMIND_EMAIL
 *  - TRAFFICMIND_API_KEY
 *  - TRAFFICMIND_BASE_URL (optional, e.g. https://api.trafficmind.com/public/v1/)
 */
public final class ExampleMain {
    private ExampleMain() {
    }

    public static void main(String[] args) {
        String email = System.getenv("TRAFFICMIND_EMAIL");
        String key = System.getenv("TRAFFICMIND_API_KEY");
        String base = System.getenv("TRAFFICMIND_BASE_URL");

        if (email == null || email.isBlank() || key == null || key.isBlank()) {
            System.err.println("Missing env vars: TRAFFICMIND_EMAIL and/or TRAFFICMIND_API_KEY");
            System.exit(2);
        }

        String normalizedBase = (base == null || base.isBlank()) ? "https://api.trafficmind.com/public/v1/" : base.trim();
        boolean allowInsecureForLocal = normalizedBase.startsWith("http://localhost")
                || normalizedBase.startsWith("http://127.0.0.1");

        TrafficmindClient client = TrafficmindClient.builder()
                .email(email)
                .apiKey(key)
                .baseUrl(normalizedBase)
                .allowInsecureTransportForTesting(allowInsecureForLocal)
                .build();

        System.out.println("Trafficmind SDK smoke check");
        System.out.println("Base URL: " + normalizedBase);
        try {
            ApiResponse<java.util.List<ResponseDomainRecord>> response = client.domains().list(DomainListRequest.create().pageSize(5).page(1));
            java.util.List<ResponseDomainRecord> domains = response.getResult();
            System.out.println("SDK OK: request completed, domains returned = " + domains.size());
            System.out.println("request_id=" + (response.getMeta() == null ? "n/a" : response.getMeta().getRequestId()));
            System.out.println("timestamp=" + (response.getMeta() == null ? "n/a" : response.getMeta().getTimestamp()));
            System.out.println("status.code=" + (response.getStatus() == null ? "n/a" : response.getStatus().getCode()));
            System.out.println("status.message=" + (response.getStatus() == null ? "n/a" : response.getStatus().getMessage()));
            System.out.println("pagination=" + (response.getResultInfo() == null ? "n/a" : response.getResultInfo()));
            for (ResponseDomainRecord z : domains) {
                System.out.println("- domain id=" + z.getId() + ", name=" + z.getName());
            }
        } catch (TrafficmindException ex) {
            System.err.println("SDK FAILED: API call error");
            System.err.println("status=" + ex.getStatusCode() + ", message=" + ex.getMessage());
            if (ex.getApiErrors() != null && !ex.getApiErrors().isEmpty()) {
                System.err.println("apiErrors=" + ex.getApiErrors());
            }
            System.exit(1);
        } catch (RuntimeException ex) {
            System.err.println("SDK FAILED: unexpected runtime error: " + ex.getMessage());
            System.exit(1);
        }
    }
}

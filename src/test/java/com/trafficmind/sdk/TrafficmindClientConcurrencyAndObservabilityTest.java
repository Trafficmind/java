package com.trafficmind.sdk;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class TrafficmindClientConcurrencyAndObservabilityTest {

    @Test
    void supportsConcurrentRequests() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[]}}"));

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(0, null)
                    .build();

            int workers = 20;
            int iterationsPerWorker = 5;
            CountDownLatch done = new CountDownLatch(workers);
            ExecutorService pool = Executors.newFixedThreadPool(workers);

            for (int i = 0; i < workers; i++) {
                pool.submit(() -> {
                    try {
                        for (int j = 0; j < iterationsPerWorker; j++) {
                            assertNotNull(client.domains().list(null));
                        }
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertTrue(done.await(10, TimeUnit.SECONDS));
            pool.shutdownNow();
        }
    }

    @Test
    void emitsObserverEvents() throws Exception {
        try (MockServer server = new MockServer()) {
            server.when("GET", "/public/v1/domains", ex -> MockServer.writeJson(ex, 200, "{\"status\":{\"code\":\"ok\"},\"payload\":{\"items\":[]}}"));

            AtomicInteger onRequest = new AtomicInteger();
            AtomicInteger onResponse = new AtomicInteger();
            AtomicInteger onError = new AtomicInteger();

            SdkEventListener listener = new SdkEventListener() {
                @Override
                public void onRequest(String url, String method, int attempt) {
                    onRequest.incrementAndGet();
                }

                @Override
                public void onResponse(String url, int statusCode, int attempt, long durationMs, long timestampMs) {
                    onResponse.incrementAndGet();
                    assertTrue(durationMs >= 0);
                }

                @Override
                public void onError(String url, TrafficmindException exception, int attempt, long durationMs, long timestampMs) {
                    onError.incrementAndGet();
                }
            };

            TrafficmindClient client = TrafficmindClient.builder()
                    .email("e")
                    .apiKey("k")
                    .baseUrl(server.baseUri())
                    .allowInsecureTransportForTesting(true)
                    .withRetries(1, Duration.ofMillis(1))
                    .withEventListener(listener)
                    .build();

            assertEquals(0, client.domains().list(null).getResult().size());
            assertEquals(1, onRequest.get());
            assertEquals(1, onResponse.get());
            assertEquals(0, onError.get());
        }
    }
}

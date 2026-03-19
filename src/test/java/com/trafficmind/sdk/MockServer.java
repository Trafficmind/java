package com.trafficmind.sdk;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Minimal dispatcher-based HTTP server for unit tests.
 */
final class MockServer implements AutoCloseable {
    private static final String TRANSFORMER_NAME = "dynamic-dispatch-transformer";

    private final WireMockServer server;
    private final Map<String, Handler> routes = new ConcurrentHashMap<>();

    interface Handler {
        void handle(MockExchange ex) throws IOException;
    }

    MockServer() {
        server = new WireMockServer(options().dynamicPort().extensions(new DispatchTransformer(routes)));
        server.start();
    }

    int port() {
        return server.port();
    }

    String baseUri() {
        return "http://localhost:" + port() + "/public/v1/";
    }

    void when(String method, String path, Handler handler) {
        String key = routeKey(method, path);
        routes.put(key, handler);
        server.stubFor(request(method.toUpperCase(), urlPathEqualTo(path))
                .willReturn(aResponse().withTransformers(TRANSFORMER_NAME)));
    }

    @Override
    public void close() {
        server.stop();
    }

    static String readBody(MockExchange ex) {
        return ex.getRequestBody();
    }

    static void assertHeader(MockExchange ex, String name, String expected) {
        String v = ex.getRequestHeaders().getFirst(name);
        if (v == null || !v.equals(expected)) {
            throw new AssertionError("Expected header " + name + "=" + expected + " but was " + v);
        }
    }

    static void writeJson(MockExchange ex, int status, String json) {
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.setResponse(status, json);
    }

    static final class MockHeaders {
        private final Map<String, List<String>> values = new LinkedHashMap<>();

        void add(String name, String value) {
            values.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        String getFirst(String name) {
            for (Map.Entry<String, List<String>> e : values.entrySet()) {
                if (e.getKey().equalsIgnoreCase(name) && !e.getValue().isEmpty()) {
                    return e.getValue().get(0);
                }
            }
            return null;
        }

        Map<String, List<String>> asMap() {
            return values;
        }
    }

    static final class MockExchange {
        private final URI requestUri;
        private final String requestMethod;
        private final String requestBody;
        private final MockHeaders requestHeaders;
        private final MockHeaders responseHeaders = new MockHeaders();
        private int responseStatus = 200;
        private String responseBody = "";

        MockExchange(URI requestUri, String requestMethod, String requestBody, MockHeaders requestHeaders) {
            this.requestUri = requestUri;
            this.requestMethod = requestMethod;
            this.requestBody = requestBody;
            this.requestHeaders = requestHeaders;
        }

        URI getRequestURI() {
            return requestUri;
        }

        String getRequestMethod() {
            return requestMethod;
        }

        MockHeaders getRequestHeaders() {
            return requestHeaders;
        }

        MockHeaders getResponseHeaders() {
            return responseHeaders;
        }

        String getRequestBody() {
            return requestBody;
        }

        int getResponseStatus() {
            return responseStatus;
        }

        String getResponseBody() {
            return responseBody;
        }

        void setResponse(int responseStatus, String responseBody) {
            this.responseStatus = responseStatus;
            this.responseBody = responseBody;
        }
    }

    private static final class DispatchTransformer implements ResponseDefinitionTransformerV2 {
        private final Map<String, Handler> routes;

        DispatchTransformer(Map<String, Handler> routes) {
            this.routes = routes;
        }

        @Override
        public String getName() {
            return TRANSFORMER_NAME;
        }

        @Override
        public ResponseDefinition transform(ServeEvent serveEvent) {
            var req = serveEvent.getRequest();
            URI uri = URI.create("http://localhost" + req.getUrl());
            String key = routeKey(req.getMethod().getName(), uri.getPath());

            Handler handler = routes.get(key);
            if (handler == null) {
                return ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"not found\"}")
                        .build();
            }

            MockHeaders requestHeaders = new MockHeaders();
            for (String headerName : req.getAllHeaderKeys()) {
                for (String value : req.header(headerName).values()) {
                    requestHeaders.add(headerName, value);
                }
            }

            MockExchange ex = new MockExchange(
                    uri,
                    req.getMethod().getName(),
                    req.getBodyAsString(),
                    requestHeaders
            );

            try {
                handler.handle(ex);
            } catch (IOException e) {
                return ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"" + e.getMessage() + "\"}")
                        .build();
            }

            ResponseDefinitionBuilder rb = ResponseDefinitionBuilder.responseDefinition()
                    .withStatus(ex.getResponseStatus())
                    .withBody(ex.getResponseBody().getBytes(StandardCharsets.UTF_8));

            ex.getResponseHeaders().asMap().forEach((name, values) -> {
                for (String value : values) {
                    rb.withHeader(name, value);
                }
            });

            return rb.build();
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }

    }

    private static String routeKey(String method, String path) {
        return method.toUpperCase() + " " + path;
    }
}

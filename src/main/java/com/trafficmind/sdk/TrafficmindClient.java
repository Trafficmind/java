package com.trafficmind.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.trafficmind.sdk.endpoint.CdnApi;
import com.trafficmind.sdk.endpoint.DnsApi;
import com.trafficmind.sdk.endpoint.DomainSettingsApi;
import com.trafficmind.sdk.endpoint.DomainsApi;
import com.trafficmind.sdk.endpoint.WafApi;
import com.trafficmind.sdk.internal.QueryParams;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ApiResponseStatus;
import com.trafficmind.sdk.dto.ResponseMeta;
import com.trafficmind.sdk.dto.ResultInfo;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Main entry point for Trafficmind Client API.
 *
 * <p>This client is thread-safe and should be shared across threads.
 * Create one instance per credential/config set and reuse it.</p>
 *
 * <p>Use {@link #builder()} to configure credentials, base URL, timeouts, retry behavior,
 * TLS/proxy settings and observability hooks. The default builder values are production-safe:
 * HTTPS-only transport, connect timeout 15s, request timeout 30s, retry attempts 2, and
 * max response body size 10 MB.</p>
 *
 * <p>Endpoint wrappers are created via {@link #domains()}, {@link #dns()},
 * {@link #cdn()}, {@link #waf()}, and {@link #domainSettings()}.</p>
 */
public final class TrafficmindClient {
    private static final String DEFAULT_BASE_URI = "https://api.trafficmind.com/public/v1/";
    private static final int DEFAULT_MAX_RESPONSE_BYTES = 10 * 1024 * 1024;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(300);
    private static final Duration MAX_RETRY_DELAY = Duration.ofSeconds(30);
    private static final int MAX_ALLOWED_RETRIES = 10;
    private static final String DEFAULT_USER_AGENT = "trafficmind-java-sdk/" + resolveSdkVersion();

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final ResponseCodec responseCodec;
    private final SdkEventListener eventListener;

    private final String email;
    private final String apiKey;
    private final String baseUri;

    private final Duration requestTimeout;
    private final int maxRetries;
    private final Duration initialRetryDelay;
    private final int maxResponseBytes;
    private final String idempotencyKey;

    public TrafficmindClient(String email, String apiKey) {
        this(builder().email(email).apiKey(apiKey));
    }

    /**
     * Creates a client using explicit base URL override.
     */
    public TrafficmindClient(String email, String apiKey, String baseUri) {
        this(builder().email(email).apiKey(apiKey).baseUrl(baseUri));
    }

    private TrafficmindClient(Builder builder) {
        if (builder.email == null || builder.email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (builder.apiKey == null || builder.apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey is required");
        }
        if (builder.maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (builder.maxRetries > MAX_ALLOWED_RETRIES) {
            throw new IllegalArgumentException("maxRetries must be <= " + MAX_ALLOWED_RETRIES);
        }
        if (builder.maxResponseBytes <= 0) {
            throw new IllegalArgumentException("maxResponseBytes must be > 0");
        }

        this.email = builder.email;
        this.apiKey = builder.apiKey;
        this.baseUri = normalizeBaseUri(builder.baseUri, builder.allowInsecureTransport);

        this.requestTimeout = builder.requestTimeout;
        this.maxRetries = builder.maxRetries;
        this.initialRetryDelay = builder.initialRetryDelay;
        this.maxResponseBytes = builder.maxResponseBytes;
        this.idempotencyKey = builder.idempotencyKey;
        this.eventListener = builder.eventListener == null ? SdkEventListener.NOOP : builder.eventListener;

        if (builder.httpClient != null) {
            this.http = builder.httpClient;
        } else {
            HttpClient.Builder httpBuilder = HttpClient.newBuilder()
                    .connectTimeout(builder.connectTimeout);
            if (builder.proxySelector != null) {
                httpBuilder.proxy(builder.proxySelector);
            }
            if (builder.sslContext != null) {
                httpBuilder.sslContext(builder.sslContext);
            }
            this.http = httpBuilder.build();
        }

        this.mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.responseCodec = new ResponseCodec(this.mapper);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String normalizeBaseUri(String baseUri, boolean allowInsecureTransport) {
        URI uri = URI.create(withDefaultScheme(baseUri));
        String scheme = validateAndNormalizeScheme(uri.getScheme(), allowInsecureTransport);
        String authority = validateAuthority(uri.getRawAuthority());
        String path = normalizeApiPath(Optional.ofNullable(uri.getPath()).orElse(""));
        return scheme + "://" + authority + path;
    }

    private static String withDefaultScheme(String baseUri) {
        String raw = (baseUri == null || baseUri.isBlank()) ? DEFAULT_BASE_URI : baseUri.trim();
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        return "https://" + raw;
    }

    private static String validateAndNormalizeScheme(String scheme, boolean allowInsecureTransport) {
        if (scheme == null) {
            throw new IllegalArgumentException("baseUri must include a valid URL scheme");
        }
        String normalized = scheme.toLowerCase();
        if ("http".equals(normalized)) {
            if (!allowInsecureTransport) {
                throw new IllegalArgumentException("baseUri must use https://");
            }
            return normalized;
        }
        if ("https".equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("baseUri must use http:// or https:// scheme");
    }

    private static String validateAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            throw new IllegalArgumentException("baseUri must include host");
        }
        return authority;
    }

    private static String normalizeApiPath(String path) {
        if (!path.isBlank() && !"/".equals(path) && !path.contains("/public/v1")) {
            throw new IllegalArgumentException("baseUri must target /public/v1 API path");
        }
        String normalized = path;
        if (!normalized.contains("/public/v1")) {
            if (!normalized.endsWith("/")) {
                normalized += "/";
            }
            normalized += "public/v1/";
        }
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        return normalized;
    }

    public DomainsApi domains() {
        return new DomainsApi(this);
    }

    public CdnApi cdn() {
        return new CdnApi(this);
    }

    public DnsApi dns() {
        return new DnsApi(this);
    }

    public WafApi waf() {
        return new WafApi(this);
    }

    public DomainSettingsApi domainSettings() {
        return new DomainSettingsApi(this);
    }

    public JsonNode getRaw(String path, Map<String, String> query) {
        return request("GET", path, query, null);
    }

    public JsonNode postRaw(String path, Object body) {
        return request("POST", path, null, body);
    }

    public JsonNode putRaw(String path, Object body) {
        return request("PUT", path, null, body);
    }

    public JsonNode patchRaw(String path, Object body) {
        return request("PATCH", path, null, body);
    }

    public JsonNode deleteRaw(String path) {
        return request("DELETE", path, null, null);
    }

    /**
     * Builds a relative API path from URL-safe path segments.
     *
     * <p>Each segment is percent-encoded and joined with {@code /}. Use this helper for
     * user-provided identifiers (domain ids, usernames, etc.) to avoid path injection bugs.</p>
     */
    public String path(String... segments) {
        if (segments == null || segments.length == 0) {
            throw new IllegalArgumentException("segments must not be empty");
        }
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                throw new IllegalArgumentException("path segment must not be blank");
            }
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(encodePathSegment(segment));
        }
        return sb.toString();
    }

    JsonNode request(String method, String path, Map<String, String> query, Object body) {
        String url = buildUrl(path, query);
        String bodyJson = resolveBodyJson(method, body);
        List<Throwable> retryTransportCauses = new ArrayList<>();

        int maxAttempts = maxRetries + 1;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long started = System.nanoTime();
            long startedAtMs = Instant.now().toEpochMilli();
            eventListener.onRequest(url, method, attempt);

            HttpRequest request = buildRequest(method, url, bodyJson);
            try {
                HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
                int statusCode = response.statusCode();
                String rawBody = readLimitedBody(response.body(), maxResponseBytes);
                long durationMs = Duration.ofNanos(System.nanoTime() - started).toMillis();

                JsonNode node = (statusCode >= 200 && statusCode < 300)
                        ? parseJsonStrict(rawBody, statusCode)
                        : parseJsonLenient(rawBody);
                if (isSuccessful(statusCode, node)) {
                    eventListener.onResponse(url, statusCode, attempt, durationMs, startedAtMs);
                    return node == null ? MissingNode.getInstance() : node;
                }

                List<String> apiErrors = extractErrorMessages(node);
                String apiErrorMessage = extractErrorMessage(node, statusCode);
                String message = formatFailureMessage(apiErrorMessage, attempt, maxAttempts);
                TrafficmindException failure = TrafficmindException.fromStatus(statusCode, message, rawBody, apiErrors, attempt);

                if (attempt < maxAttempts && shouldRetry(method, statusCode)) {
                    Duration delay = retryDelay(response.headers(), attempt);
                    eventListener.onError(url, failure, attempt, durationMs, startedAtMs);
                    eventListener.onRetry(url, method, attempt, delay, "status=" + statusCode + ", error=" + apiErrorMessage);
                    sleep(delay);
                    continue;
                }
                throw failure;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                long durationMs = Duration.ofNanos(System.nanoTime() - started).toMillis();
                TrafficmindException failure = new TrafficmindException(
                        "HTTP request interrupted",
                        0,
                        null,
                        List.of(),
                        false,
                        false,
                        attempt,
                        e
                );
                eventListener.onError(url, failure, attempt, durationMs, startedAtMs);
                throw failure;
            } catch (TrafficmindException e) {
                long durationMs = Duration.ofNanos(System.nanoTime() - started).toMillis();
                eventListener.onError(url, e, attempt, durationMs, startedAtMs);
                throw e;
            } catch (IOException e) {
                long durationMs = Duration.ofNanos(System.nanoTime() - started).toMillis();
                TrafficmindException failure = new TrafficmindException(
                        formatFailureMessage("HTTP transport error: " + sanitizeThrowableMessage(e.getMessage()), attempt, maxAttempts),
                        0,
                        null,
                        List.of(),
                        true,
                        true,
                        attempt,
                        e
                );
                eventListener.onError(url, failure, attempt, durationMs, startedAtMs);
                retryTransportCauses.add(e);

                if (attempt < maxAttempts && shouldRetryTransport(method)) {
                    Duration delay = retryDelay(HttpHeaders.of(Map.of(), (k, v) -> true), attempt);
                    String reason = sanitizeThrowableMessage(e.getMessage());
                    eventListener.onRetry(url, method, attempt, delay, reason.isBlank() ? "io_error" : "io_error: " + reason);
                    sleep(delay);
                    continue;
                }
                addSuppressedRetryCauses(failure, retryTransportCauses);
                throw failure;
            }
        }

        throw new IllegalStateException("unreachable");
    }

    private boolean isSuccessful(int statusCode, JsonNode node) {
        return statusCode >= 200 && statusCode < 300;
    }

    private HttpRequest buildRequest(String method, String url, String bodyJson) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(requestTimeout)
                .header("User-Agent", DEFAULT_USER_AGENT)
                .header("X-Access-User", email)
                .header("X-Access-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        if (("POST".equals(method) || "PATCH".equals(method)) && idempotencyKey != null && !idempotencyKey.isBlank()) {
            b.header("X-Idempotency-Key", idempotencyKey);
        }

        if (bodyJson == null) {
            b.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            b.method(method, HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8));
        }
        return b.build();
    }

    private String buildUrl(String path, Map<String, String> query) {
        String p = stripLeadingSlash(path);
        String url = baseUri + p;
        if (query != null && !query.isEmpty()) {
            String qs = QueryParams.toQueryString(query);
            if (!qs.isEmpty()) {
                url += "?" + qs;
            }
        }
        return url;
    }

    private String resolveBodyJson(String method, Object body) {
        if (body != null) {
            return toJson(body);
        }
        return null;
    }

    private static void sleep(Duration delay) {
        if (delay.isZero() || delay.isNegative()) {
            return;
        }
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TrafficmindException(
                    "Retry sleep interrupted",
                    0,
                    null,
                    List.of(),
                    false,
                    false,
                    0,
                    e
            );
        }
    }

    private static void addSuppressedRetryCauses(TrafficmindException failure, List<Throwable> causes) {
        if (causes == null || causes.size() <= 1) {
            return;
        }
        Throwable primary = failure.getCause();
        for (Throwable cause : causes) {
            if (cause != null && cause != primary) {
                failure.addSuppressed(cause);
            }
        }
    }

    private Duration retryDelay(HttpHeaders headers, int attempt) {
        Optional<String> retryAfter = headers.firstValue("Retry-After");
        Duration fromHeader = retryAfter.map(TrafficmindClient::parseRetryAfter).orElse(null);
        if (fromHeader != null && !fromHeader.isNegative()) {
            return fromHeader;
        }

        long base = initialRetryDelay.toMillis();
        long exp = base * (1L << Math.max(0, attempt - 1));
        long capped = Math.min(exp, MAX_RETRY_DELAY.toMillis());
        long jitter = ThreadLocalRandom.current().nextLong(0, 200);
        return Duration.ofMillis(capped + jitter);
    }

    static Duration parseRetryAfter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        try {
            return Duration.ofSeconds(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            try {
                OffsetDateTime at = OffsetDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
                long seconds = Duration.between(Instant.now(), at.toInstant()).getSeconds();
                return seconds <= 0 ? Duration.ZERO : Duration.ofSeconds(seconds);
            } catch (RuntimeException ignoredDateParse) {
                return null;
            }
        }
    }

    private static boolean shouldRetryStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private boolean shouldRetry(String method, int statusCode) {
        return shouldRetryStatus(statusCode) && shouldRetryTransport(method);
    }

    private boolean shouldRetryTransport(String method) {
        return isRetryableMethod(method) || hasIdempotencyGuarantee(method);
    }

    private static boolean isRetryableMethod(String method) {
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    private boolean hasIdempotencyGuarantee(String method) {
        boolean idempotencyEnabled = idempotencyKey != null && !idempotencyKey.isBlank();
        return idempotencyEnabled && ("POST".equals(method) || "PATCH".equals(method));
    }

    private static String readLimitedBody(InputStream bodyStream, int maxBytes) throws IOException {
        if (bodyStream == null) {
            return "";
        }

        int hardLimit = maxBytes + 1;
        byte[] buf = new byte[8192];
        int total = 0;
        try (InputStream in = bodyStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int n;
            while ((n = in.read(buf)) != -1) {
                total += n;
                if (total > hardLimit) {
                    throw new TrafficmindException(
                            "Response body exceeds configured limit of " + maxBytes + " bytes",
                            0,
                            null,
                            List.of(),
                            false,
                            false,
                            0,
                            null
                    );
                }
                out.write(buf, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static String stripLeadingSlash(String path) {
        if (path == null) {
            return "";
        }
        String p = path.trim();
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p;
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String toJson(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new TrafficmindException("Failed to serialize request body: " + sanitizeThrowableMessage(e.getMessage()), 0, e);
        }
    }

    private JsonNode parseJsonStrict(String raw, int statusCode) {
        if (raw == null) {
            return MissingNode.getInstance();
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return MissingNode.getInstance();
        }
        try {
            return mapper.readTree(s);
        } catch (IOException e) {
            throw new TrafficmindException(
                    sanitizeThrowableMessage("Failed to decode API response as JSON"),
                    statusCode,
                    raw,
                    List.of(),
                    false,
                    false,
                    0,
                    e
            );
        }
    }

    private String formatFailureMessage(String base, int attempt, int maxAttempts) {
        String sanitized = sanitizeThrowableMessage(base);
        if (attempt > 1 || maxAttempts > 1) {
            return sanitized + " (after " + attempt + " attempt" + (attempt > 1 ? "s" : "") + ")";
        }
        return sanitized;
    }

    private String sanitizeThrowableMessage(String message) {
        if (message == null) {
            return "";
        }
        String sanitized = message
                .replace(apiKey, "[REDACTED]")
                .replace(email, "[REDACTED]");
        return sanitized
                .replace("X-Access-Key", "X-Access-Key:[REDACTED]")
                .replace("X-Access-User", "X-Access-User:[REDACTED]")
                .replace("X-Auth-Key", "X-Auth-Key:[REDACTED]")
                .replace("X-Auth-Email", "X-Auth-Email:[REDACTED]");
    }

    private static String resolveSdkVersion() {
        Package p = TrafficmindClient.class.getPackage();
        String version = p == null ? null : p.getImplementationVersion();
        if (version == null || version.isBlank()) {
            return "1.0.0";
        }
        return version;
    }

    private JsonNode parseJsonLenient(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return MissingNode.getInstance();
        }
        try {
            return mapper.readTree(raw);
        } catch (IOException ignored) {
            return MissingNode.getInstance();
        }
    }

    private static List<String> extractErrorMessages(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return List.of();
        }

        List<String> messages = new ArrayList<>();
        JsonNode error = node.path("error");
        String errorMessage = error.path("message").asText(null);
        if (errorMessage != null && !errorMessage.isBlank()) {
            messages.add(errorMessage);
        }
        JsonNode details = error.path("details");
        if (details.isArray()) {
            for (JsonNode detail : details) {
                String field = detail.path("field").asText(null);
                String msg = detail.path("message").asText(null);
                if (msg != null && !msg.isBlank()) {
                    messages.add(field == null || field.isBlank() ? msg : field + ": " + msg);
                }
            }
        }
        return messages;
    }

    private static String extractErrorMessage(JsonNode node, int statusCode) {
        List<String> messages = extractErrorMessages(node);
        if (!messages.isEmpty()) {
            return String.join("; ", messages);
        }

        if (node != null && !node.isMissingNode()) {
            String errorMessage = node.path("error").path("message").asText(null);
            if (errorMessage != null && !errorMessage.isBlank()) {
                return errorMessage;
            }
            String statusMessage = node.path("status").path("message").asText(null);
            if (statusMessage != null && !statusMessage.isBlank()) {
                return statusMessage;
            }
        }
        return "HTTP " + statusCode;
    }

    public <T> T readResult(JsonNode root, Class<T> clazz) {
        return responseCodec.readResult(root, clazz);
    }

    public <T> T readBody(JsonNode root, Class<T> clazz) {
        return responseCodec.readBody(root, clazz);
    }

    public JsonNode extractResultNode(JsonNode root) {
        return responseCodec.extractResultNode(root);
    }

    public JsonNode extractResultInfoNode(JsonNode root) {
        return responseCodec.extractResultInfoNode(root);
    }

    public JsonNode extractPayloadNode(JsonNode root) {
        return responseCodec.extractPayloadNode(root);
    }

    public JsonNode extractResponseMetaNode(JsonNode root) {
        return responseCodec.extractResponseMetaNode(root);
    }

    public JsonNode extractResponseStatusNode(JsonNode root) {
        return responseCodec.extractResponseStatusNode(root);
    }

    public ResultInfo readResultInfo(JsonNode root) {
        return responseCodec.readResultInfo(root);
    }

    public ResponseMeta readResponseMeta(JsonNode root) {
        return responseCodec.readResponseMeta(root);
    }

    public ApiResponseStatus readResponseStatus(JsonNode root) {
        return responseCodec.readResponseStatus(root);
    }

    public <T> ApiResponse<T> readApiResponse(JsonNode root, Class<T> clazz) {
        return responseCodec.readApiResponse(root, clazz);
    }

    public <T> ApiResponse<T> readApiPayloadResponse(JsonNode root, Class<T> clazz) {
        return responseCodec.readApiPayloadResponse(root, clazz);
    }

    public <T> ApiResponse<List<T>> readApiResponseList(JsonNode root, Class<T> itemClass) {
        return responseCodec.readApiResponseList(root, itemClass);
    }

    public <T> List<T> readResultList(JsonNode root, Class<T> itemClass) {
        return responseCodec.readResultList(root, itemClass);
    }

    /**
     * Returns the configured shared mapper.
     *
     * <p>The mapper is configured once in the client constructor and then used concurrently.
     * Jackson {@link ObjectMapper} is thread-safe after configuration.</p>
     */
    ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static final class Builder {
        private String email;
        private String apiKey;
        private String baseUri;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        private int maxRetries = 2;
        private Duration initialRetryDelay = DEFAULT_RETRY_DELAY;
        private int maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;
        private HttpClient httpClient;
        private ProxySelector proxySelector;
        private SSLContext sslContext;
        private SdkEventListener eventListener = SdkEventListener.NOOP;
        private boolean allowInsecureTransport;
        private String idempotencyKey;

        private Builder() {
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        /**
         * Sets both connect and request timeout at once.
         */
        public Builder withTimeout(Duration connectTimeout, Duration requestTimeout) {
            if (connectTimeout != null) {
                validateTimeout(connectTimeout, "connectTimeout");
                this.connectTimeout = connectTimeout;
            }
            if (requestTimeout != null) {
                validateTimeout(requestTimeout, "requestTimeout");
                this.requestTimeout = requestTimeout;
            }
            return this;
        }

        /**
         * Sets connect timeout only.
         */
        public Builder connectTimeout(Duration connectTimeout) {
            if (connectTimeout != null) {
                validateTimeout(connectTimeout, "connectTimeout");
                this.connectTimeout = connectTimeout;
            }
            return this;
        }

        /**
         * Sets request timeout only.
         */
        public Builder requestTimeout(Duration requestTimeout) {
            if (requestTimeout != null) {
                validateTimeout(requestTimeout, "requestTimeout");
                this.requestTimeout = requestTimeout;
            }
            return this;
        }

        /**
         * Configures retry attempts and initial retry delay.
         * maxRetries must be in range 0..10.
         */
        public Builder withRetries(int maxRetries, Duration initialDelay) {
            if (maxRetries > MAX_ALLOWED_RETRIES) {
                throw new IllegalArgumentException("maxRetries must be <= " + MAX_ALLOWED_RETRIES);
            }
            this.maxRetries = maxRetries;
            if (initialDelay != null) {
                validateTimeout(initialDelay, "initialDelay");
                this.initialRetryDelay = initialDelay;
            }
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            if (maxRetries > MAX_ALLOWED_RETRIES) {
                throw new IllegalArgumentException("maxRetries must be <= " + MAX_ALLOWED_RETRIES);
            }
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            // Custom HttpClient takes precedence over proxy/tls/connectTimeout builder options.
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets a custom proxy selector for HTTP client.
         */
        public Builder withProxy(ProxySelector proxySelector) {
            this.proxySelector = proxySelector;
            return this;
        }

        /**
         * Sets SSL context for custom trust/mTLS scenarios.
         */
        public Builder withTlsConfig(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Registers observability listener invoked on request lifecycle events.
         * Implementations must be thread-safe.
         */
        public Builder withEventListener(SdkEventListener eventListener) {
            this.eventListener = eventListener == null ? SdkEventListener.NOOP : eventListener;
            return this;
        }

        /**
         * Sets max response body bytes before failing request processing.
         */
        public Builder withMaxResponseBytes(int maxResponseBytes) {
            this.maxResponseBytes = maxResponseBytes;
            return this;
        }

        /**
         * Adds X-Idempotency-Key header to POST and PATCH requests.
         */
        public Builder withIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        /**
         * Allows HTTP URLs only for tests and local mocks.
         */
        public Builder allowInsecureTransportForTesting(boolean allowInsecureTransport) {
            this.allowInsecureTransport = allowInsecureTransport;
            return this;
        }

        /**
         * Builds immutable thread-safe client instance.
         */
        public TrafficmindClient build() {
            return new TrafficmindClient(this);
        }

        private static void validateTimeout(Duration timeout, String fieldName) {
            Objects.requireNonNull(fieldName, "fieldName");
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException(fieldName + " must be > 0");
            }
        }
    }
}

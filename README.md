# Trafficmind Java SDK

Official Java SDK for the Trafficmind API (`/public/v1`).

This SDK provides typed access to:
- domains
- DNS records
- CDN storages and SFTP users
- WAF access rules
- domain settings

## Table of contents

- [Enterprise readiness summary](#enterprise-readiness-summary)
- [Requirements](#requirements)
- [Installation](#installation)
- [Authentication](#authentication)
- [Quick start](#quick-start)
- [Client construction](#client-construction)
- [Configuration reference](#configuration-reference)
- [Resource map](#resource-map)
- [Usage examples](#usage-examples)
- [Pagination model](#pagination-model)
- [Error model](#error-model)
- [Retries](#retries)
- [Timeouts](#timeouts)
- [Observability](#observability)
- [Transport, proxy, and TLS customization](#transport-proxy-and-tls-customization)
- [Concurrency and lifecycle guidance](#concurrency-and-lifecycle-guidance)
- [Response size protection](#response-size-protection)
- [Path safety](#path-safety)
- [Running the included example](#running-the-included-example)
- [Build, test, and verification](#build-test-and-verification)
- [Security](#security)
- [Compatibility, versioning, and upgrades](#compatibility-versioning-and-upgrades)
- [Changelog](#changelog)
- [License](#license)

## Enterprise readiness summary

This SDK is designed for production service environments:

- **Thread-safe and reusable** — immutable `TrafficmindClient` intended to be shared across threads.
- **Transport-safe by default** — HTTPS enforced by default, bounded response body size, explicit retry limits.
- **Operationally predictable** — no background workers, no hidden global state, no internal credential cache.
- **Observable** — request lifecycle hooks available through `SdkEventListener`.
- **JDK-native** — built on `java.net.http.HttpClient` with Jackson-based DTO mapping.
- **CI-friendly** — Maven verification pipeline includes tests, coverage, static analysis, and packaging checks.

> Default API endpoint: `https://api.trafficmind.com`
>
> Requests are sent to: `/public/v1/...`

## Requirements

- Java **17+**
- Maven **3.8+**

## Installation

Use the Maven coordinates defined by this project:

```xml
<dependency>
  <groupId>com.trafficmind</groupId>
  <artifactId>trafficmind-java-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

Production recommendation: pin an explicit SDK version and upgrade intentionally through your normal release process.

### Build from source

```bash
git clone <your-repository-url>
cd trafficmind-java
mvn -q test
mvn -q package
```

Build artifacts are produced under `target/`.

## Authentication

Trafficmind uses header-based authentication:

- `X-Access-User`
- `X-Access-Key`

The SDK injects these headers automatically for every request.
Create a separate `TrafficmindClient` per credential set.

Typical environment variables:

```bash
export TRAFFICMIND_EMAIL="you@example.com"
export TRAFFICMIND_API_KEY="<GLOBAL_API_KEY>"
# optional:
export TRAFFICMIND_BASE_URL="https://api.trafficmind.com"
```

## Quick start

```java
import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.request.DomainListRequest;

TrafficmindClient client = new TrafficmindClient(
    System.getenv("TRAFFICMIND_EMAIL"),
    System.getenv("TRAFFICMIND_API_KEY")
);

ApiResponse<java.util.List<ResponseDomainRecord>> response = client.domains().list(
    DomainListRequest.create()
        .page(1)
        .pageSize(20)
);

System.out.println("domains: " + response.getResult().size());
System.out.println("request_id: " + response.getMeta().getRequestId());
System.out.println("status: " + response.getStatus().getCode());
```

## Client construction

### Default configuration

```java
import com.trafficmind.sdk.TrafficmindClient;

TrafficmindClient client = new TrafficmindClient(email, apiKey);
```

### Custom base URL

```java
TrafficmindClient client = new TrafficmindClient(
    email,
    apiKey,
    "https://staging.api.trafficmind.example"
);
```

The client normalizes the base URL so requests target `/public/v1/`.
Both of the following inputs are accepted:

- `https://api.trafficmind.com`
- `https://api.trafficmind.com/public/v1/`

### Builder API

For enterprise deployments, prefer the builder because it exposes transport, timeout, retry, TLS, proxy, idempotency, and observability controls.

```java
import com.trafficmind.sdk.TrafficmindClient;

import java.time.Duration;

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .baseUrl("https://api.trafficmind.com")
    .connectTimeout(Duration.ofSeconds(5))
    .requestTimeout(Duration.ofSeconds(20))
    .maxRetries(2)
    .build();
```

## Configuration reference

### Builder options

| Option | Purpose |
|---|---|
| `baseUrl(...)` | Override API host or full API prefix |
| `withTimeout(connectTimeout, requestTimeout)` | Set both timeouts together |
| `connectTimeout(...)` | TCP/TLS connect timeout |
| `requestTimeout(...)` | Whole-request timeout |
| `withRetries(maxRetries, initialDelay)` | Configure retry count and initial backoff |
| `maxRetries(...)` | Set retry count only |
| `httpClient(...)` | Inject a custom `HttpClient` |
| `withProxy(...)` | Configure `ProxySelector` |
| `withTlsConfig(...)` | Inject custom `SSLContext` |
| `withEventListener(...)` | Register request lifecycle hooks |
| `withMaxResponseBytes(...)` | Cap response size in bytes |
| `withIdempotencyKey(...)` | Add `X-Idempotency-Key` to `POST` and `PATCH` |
| `allowInsecureTransportForTesting(...)` | Allow `http://` only for local tests and mocks |

### Defaults

Default client behavior:

- base URL: `https://api.trafficmind.com/public/v1/`
- connect timeout: **15s**
- request timeout: **30s**
- max retries: **2**
- initial retry delay: **300ms**
- max retry delay cap: **30s**
- max response body size: **10 MB**
- user agent: `trafficmind-java-sdk/<version>`

## Resource map

Top-level service groups exposed by the client:

- `client.domains()`
- `client.dns()`
- `client.cdn()`
- `client.waf()`
- `client.domainSettings()`

### Resource capabilities

| Resource | Operations |
|---|---|
| `domains()` | list, list with pagination metadata, create, get, delete, stream all, list all |
| `dns()` | list, list with pagination metadata, list typed payload (`search_query`), batch update operations, stream all, list all |
| `cdn()` | list storages, create storage, delete storage, refresh storage, get SFTP credentials, create SFTP user, revoke SFTP user |
| `waf()` | create account-level and domain-level access rules |
| `domainSettings()` | get and update a domain setting |

DTOs are located under `com.trafficmind.sdk.dto`.
Request builders are located under `com.trafficmind.sdk.request`.

## Usage examples

### List domains

```java
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.request.DomainListRequest;

ApiResponse<java.util.List<ResponseDomainRecord>> response = client.domains().list(
    DomainListRequest.create()
        .query("example.com")
        .page(1)
        .pageSize(50)
);

for (ResponseDomainRecord domain : response.getResult()) {
    System.out.println(domain.getId() + " " + domain.getName());
}
System.out.println("request_id=" + response.getMeta().getRequestId());
System.out.println("status=" + response.getStatus().getCode());
```

### Get pagination metadata

```java
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.request.DomainListRequest;

ApiResponse<java.util.List<ResponseDomainRecord>> page = client.domains().listWithInfo(
    DomainListRequest.create().page(1).pageSize(20)
);

System.out.println("items=" + page.getResult().size());
System.out.println("resultInfo=" + page.getResultInfo());
System.out.println("request_id=" + page.getMeta().getRequestId());
System.out.println("status=" + page.getStatus().getCode());
```

### Iterate all domains lazily

```java
client.domains()
    .streamAll(DomainListRequest.create().pageSize(50))
    .forEach(item -> System.out.println(item.getResult().getName() + " request_id=" + item.getMeta().getRequestId()));
```

### Collect all domains eagerly

```java
var allDomains = client.domains().listAll(
    DomainListRequest.create().pageSize(50)
);
System.out.println("count=" + allDomains.getResult().size());
System.out.println("last_request_id=" + allDomains.getMeta().getRequestId());
```

### List DNS records for a domain

```java
import com.trafficmind.sdk.request.DnsListRequest;

String domainId = "domain_123";

var records = client.dns().list(
    DnsListRequest.create()
        .query("all")
        .page(1)
        .pageSize(50),
    domainId
);

System.out.println("dns records=" + records.getResult().size());
System.out.println("request_id=" + records.getMeta().getRequestId());
System.out.println("search_query=" + records.getPayload().path("search_query").asText(null));
```

### List DNS records with typed payload (`search_query`)

```java
import com.trafficmind.sdk.dto.DomainRecordListPayload;
import com.trafficmind.sdk.request.DnsListRequest;

String domainId = "domain_123";

var response = client.dns().listWithPayload(
    DnsListRequest.create().query("api.example.com").page(1).pageSize(50),
    domainId
);

DomainRecordListPayload payload = response.getResult();
System.out.println("records=" + payload.getRecords().size());
System.out.println("search_query=" + payload.getSearchQuery());
```

### Create a domain

```java
import com.trafficmind.sdk.dto.CreateDomainRequest;

CreateDomainRequest request = new CreateDomainRequest();
request.setName("example.com");

var created = client.domains().create(request);
System.out.println(created.getResult().getId());
System.out.println(created.getMeta().getRequestId());
```

### Safe write requests with idempotency key

```java
import java.util.UUID;

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .withIdempotencyKey(UUID.randomUUID().toString())
    .build();
```

`X-Idempotency-Key` is attached to `POST` and `PATCH` requests only.

## Pagination model

For list endpoints, the SDK supports three patterns:

- single-page fetch: `list(...)`
- single-page fetch with explicit pagination intent: `listWithInfo(...)`
- multi-page helpers: `streamAll(...)` and `listAll(...)`

`streamAll(...)` is lazy and should be consumed sequentially.
`listAll(...)` materializes all fetched items in memory.
All methods return `ApiResponse<T>` and expose `status`, `meta` (`request_id`), and optional `resultInfo`.

## Error model

All SDK failures extend `TrafficmindException`.
Specialized subclasses include:

- `UnauthorizedException`
- `ForbiddenException`
- `NotFoundException`
- `RateLimitedException`

Available diagnostics on `TrafficmindException` include:

- `getStatusCode()`
- `getResponseBody()`
- `getApiErrors()`
- `isRetryable()`
- `isTransient()`
- `getAttemptCount()`
- `isUnauthorized()`
- `isForbidden()`
- `isNotFound()`
- `isRateLimited()`

Example:

```java
import com.trafficmind.sdk.RateLimitedException;
import com.trafficmind.sdk.TrafficmindException;
import com.trafficmind.sdk.request.DomainListRequest;

try {
    client.domains().list(DomainListRequest.create().page(1).pageSize(50));
} catch (RateLimitedException e) {
    System.err.println("rate limited after attempts=" + e.getAttemptCount());
    throw e;
} catch (TrafficmindException e) {
    System.err.println("status=" + e.getStatusCode());
    System.err.println("retryable=" + e.isRetryable());
    System.err.println("message=" + e.getMessage());
    throw e;
}
```

## Retries

Automatic retries are enabled by default.

Current retry behavior:

- default retries: **2**
- retryable statuses: `429` and `5xx`
- retries are attempted only for retry-safe methods (`GET`, `HEAD`, `OPTIONS`)
- for write operations, retries are allowed for `POST`/`PATCH` only when `withIdempotencyKey(...)` is configured
- transport I/O failures follow the same method/idempotency policy
- backoff: exponential, starting from the configured initial delay
- jitter: small random jitter added per retry
- `Retry-After` is respected when present
- maximum allowed configured retries: **10**

Example:

```java
import java.time.Duration;

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .withRetries(5, Duration.ofMillis(500))
    .build();
```

Operational recommendation: for write-heavy integrations, pair retries with idempotency keys and keep retry counts conservative.

## Timeouts

The SDK distinguishes between:

- **connect timeout** — connection establishment
- **request timeout** — full request execution

Example:

```java
import java.time.Duration;

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .connectTimeout(Duration.ofSeconds(3))
    .requestTimeout(Duration.ofSeconds(10))
    .build();
```

## Observability

The SDK does not log by default.
For metrics, tracing, or structured request logging, register `SdkEventListener`.

Callbacks:

- `onRequest(url, method, attempt)`
- `onResponse(url, statusCode, attempt, durationMs, timestampMs)`
- `onError(url, exception, attempt, durationMs, timestampMs)`
- `onRetry(url, method, attempt, delay, reason)`

Example:

```java
import com.trafficmind.sdk.SdkEventListener;
import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.TrafficmindException;

import java.time.Duration;

SdkEventListener listener = new SdkEventListener() {
    @Override
    public void onRequest(String url, String method, int attempt) {
        System.out.println("-> " + method + " " + url + " attempt=" + attempt);
    }

    @Override
    public void onResponse(String url, int statusCode, int attempt, long durationMs, long timestampMs) {
        System.out.println("<- status=" + statusCode + " durationMs=" + durationMs);
    }

    @Override
    public void onError(String url, TrafficmindException exception, int attempt, long durationMs, long timestampMs) {
        System.err.println("xx status=" + exception.getStatusCode() + " message=" + exception.getMessage());
    }

    @Override
    public void onRetry(String url, String method, int attempt, Duration delay, String reason) {
        System.out.println("retrying in " + delay.toMillis() + "ms because " + reason);
    }
};

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .withEventListener(listener)
    .build();
```

`SdkEventListener` implementations must be thread-safe.

## Transport, proxy, and TLS customization

### Custom `HttpClient`

```java
import java.net.http.HttpClient;
import java.time.Duration;

HttpClient httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build();

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .httpClient(httpClient)
    .build();
```

When a custom `HttpClient` is supplied, it takes precedence over builder-managed connect timeout, proxy, and TLS settings.

### Proxy

```java
import java.net.InetSocketAddress;
import java.net.ProxySelector;

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .withProxy(ProxySelector.of(new InetSocketAddress("proxy.internal", 8080)))
    .build();
```

### Custom TLS / trust configuration

```java
import javax.net.ssl.SSLContext;

SSLContext sslContext = SSLContext.getDefault();

TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .withTlsConfig(sslContext)
    .build();
```

### Base URL and transport safety

The SDK enforces HTTPS by default.
Plain HTTP is rejected unless explicitly enabled for local testing and mock environments.

```java
TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .baseUrl("http://localhost:8080")
    .allowInsecureTransportForTesting(true)
    .build();
```

## Concurrency and lifecycle guidance

`TrafficmindClient` is immutable and thread-safe.
Reuse a single client instance per credential and configuration set.

Recommended operating model:

- create one shared client per service process
- do not create a new client per request
- ensure custom `SdkEventListener` implementations are thread-safe
- ensure injected `HttpClient` instances are configured for concurrent use

The SDK does not start background threads or background retries.
Retries occur synchronously in the request path.

## Response size protection

To reduce the risk of unexpectedly large payloads impacting process memory, the client enforces a response size ceiling.
The default limit is **10 MB**.

Override only when required:

```java
TrafficmindClient client = TrafficmindClient.builder()
    .email(email)
    .apiKey(apiKey)
    .withMaxResponseBytes(20 * 1024 * 1024)
    .build();
```

## Path safety

When building paths from user-controlled identifiers, use `client.path(...)` rather than manual string concatenation.
This ensures path segments are URL-encoded correctly.

```java
String path = client.path("domains", domainId, "records");
```

## Running the included example

The repository includes `com.trafficmind.sdk.examples.ExampleMain` and configures Maven Exec to run it.

Environment variables used by the example:

- `TRAFFICMIND_EMAIL`
- `TRAFFICMIND_API_KEY`
- `TRAFFICMIND_BASE_URL` — optional

```bash
export TRAFFICMIND_EMAIL="you@example.com"
export TRAFFICMIND_API_KEY="<GLOBAL_API_KEY>"
# optional:
# export TRAFFICMIND_BASE_URL="https://api.trafficmind.com"

mvn -q exec:java
```

For local mocks, the example permits insecure HTTP only for `localhost` and `127.0.0.1`.

## Build, test, and verification

This project includes build and verification tooling typically expected in enterprise SDK delivery:

- JUnit 5 test suite
- WireMock-based HTTP tests
- JaCoCo coverage checks
- Maven Enforcer rules
- Checkstyle
- PMD
- SpotBugs
- source and javadoc JAR generation
- GitHub Actions CI, CodeQL, release workflow, and Scorecard workflow

Common commands:

```bash
mvn -q test
mvn -q verify
mvn -q -DskipTests package
```

`mvn verify` runs the broader validation pipeline, including static analysis and coverage gates configured in `pom.xml`.

## Security

- Treat API keys as secrets.
- Store credentials in a secret manager or environment variables.
- Do not embed credentials in mobile, browser, or other untrusted client-side applications.
- Prefer HTTPS endpoints only.
- Use `allowInsecureTransportForTesting(true)` only for local development and test infrastructure.
- Review [`SECURITY.md`](SECURITY.md) for vulnerability reporting guidance.

## Compatibility, versioning, and upgrades

- Supported runtime: Java **17+**
- Supported build tool baseline: Maven **3.8+**
- Supported API family: `/public/v1`
- Public contract: exported SDK classes and documented behavior in this README

This SDK is intended to follow Semantic Versioning.

Recommended upgrade checklist:

1. Review [`CHANGELOG.md`](CHANGELOG.md).
2. Upgrade the pinned dependency version.
3. Run `mvn test` and `mvn verify` in CI.
4. Re-validate retry, timeout, proxy, and TLS settings in the target environment.

## Changelog

See [`CHANGELOG.md`](CHANGELOG.md) for release history and breaking changes.

## License

Apache-2.0. See [`LICENSE`](LICENSE).

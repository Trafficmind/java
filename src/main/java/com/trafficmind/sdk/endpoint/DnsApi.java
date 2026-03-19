package com.trafficmind.sdk.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.BatchDNSRequest;
import com.trafficmind.sdk.dto.ResponseDnsRecordsBatchesResult;
import com.trafficmind.sdk.dto.ResultInfo;
import com.trafficmind.sdk.dto.DomainDNSRecord;
import com.trafficmind.sdk.dto.DomainRecordListPayload;
import com.trafficmind.sdk.request.DnsListRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** DNS endpoint wrapper. */
public final class DnsApi {
    private final TrafficmindClient client;

    public DnsApi(TrafficmindClient client) {
        this.client = client;
    }

    /** List, search, sort, and filter a domain's DNS records. */
    public ApiResponse<List<DomainDNSRecord>> list(DnsListRequest request, String domainId) {
        Map<String, String> q = request == null ? Map.of() : request.toQueryParams();
        JsonNode root = client.getRaw(client.path("domains", domainId, "records"), q);
        return client.readApiResponseList(root, DomainDNSRecord.class);
    }

    /** List DNS records plus pagination metadata (pagination when available). */
    public ApiResponse<List<DomainDNSRecord>> listWithInfo(DnsListRequest request, String domainId) {
        return list(request, domainId);
    }

    /**
     * List DNS records as a typed payload object, including {@code search_query}.
     */
    public ApiResponse<DomainRecordListPayload> listWithPayload(DnsListRequest request, String domainId) {
        Map<String, String> q = request == null ? Map.of() : request.toQueryParams();
        JsonNode root = client.getRaw(client.path("domains", domainId, "records"), q);
        return client.readApiPayloadResponse(root, DomainRecordListPayload.class);
    }

    /** Send a batch of DNS Record API calls to be executed together. */
    public ApiResponse<ResponseDnsRecordsBatchesResult> batchAction(BatchDNSRequest request, String domainId) {
        JsonNode root = client.postRaw(client.path("domains", domainId, "records", "batch"), request);
        return client.readApiResponse(root, ResponseDnsRecordsBatchesResult.class);
    }

    /**
     * Returns a lazy stream that requests pages on demand.
     * The stream should be consumed sequentially.
     */
    public Stream<ApiResponse<DomainDNSRecord>> streamAll(DnsListRequest request, String domainId) {
        return streamAll(request, domainId, Integer.MAX_VALUE);
    }

    /**
     * Returns a lazy stream that requests pages on demand up to maxPages.
     * The stream should be consumed sequentially.
     */
    public Stream<ApiResponse<DomainDNSRecord>> streamAll(DnsListRequest request, String domainId, int maxPages) {
        if (maxPages <= 0) {
            throw new IllegalArgumentException("maxPages must be > 0");
        }
        final DnsListRequest seed = request == null ? DnsListRequest.create() : request;
        final int startPage = seed.getPage() == null || seed.getPage() < 1 ? 1 : seed.getPage();
        final int pageSize = seed.getPageSize() == null || seed.getPageSize() < 1 ? 50 : seed.getPageSize();

        Iterator<ApiResponse<DomainDNSRecord>> iterator = new Iterator<>() {
            private int page = startPage;
            private int collectedPages = 0;
            private List<DomainDNSRecord> currentItems = List.of();
            private ApiResponse<List<DomainDNSRecord>> currentPageResponse;
            private int currentIndex = 0;
            private boolean done;

            @Override
            public boolean hasNext() {
                fetchPageIfNeeded();
                return currentIndex < currentItems.size();
            }

            @Override
            public ApiResponse<DomainDNSRecord> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                DomainDNSRecord item = currentItems.get(currentIndex++);
                return currentPageResponse.withResult(item);
            }

            private void fetchPageIfNeeded() {
                if (done || currentIndex < currentItems.size()) {
                    return;
                }
                if (Thread.currentThread().isInterrupted()) {
                    throw new IllegalStateException("Pagination interrupted");
                }
                if (collectedPages >= maxPages) {
                    done = true;
                    return;
                }
                DnsListRequest current = DnsListRequest.create()
                        .query(seed.getQuery())
                        .page(page)
                        .pageSize(pageSize);
                currentPageResponse = listWithInfo(current, domainId);
                currentItems = currentPageResponse.getResult() == null ? List.of() : currentPageResponse.getResult();
                currentIndex = 0;
                collectedPages++;

                ResultInfo info = currentPageResponse.getResultInfo();
                int totalPages = info == null || info.getTotalCount() == null || info.getPageSize() == null || info.getPage() == null
                        ? -1
                        : (int) Math.ceil((double) info.getTotalCount() / Math.max(1, info.getPageSize()));
                if (currentItems.isEmpty() || totalPages <= 0 || info.getPage() >= totalPages) {
                    done = true;
                    return;
                }
                page++;
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    /** Iterates over all DNS pages and returns the full dataset. */
    public ApiResponse<List<DomainDNSRecord>> listAll(DnsListRequest request, String domainId) {
        return listAll(request, domainId, Integer.MAX_VALUE);
    }

    /** Iterates over DNS pages up to maxPages and returns collected records. */
    public ApiResponse<List<DomainDNSRecord>> listAll(DnsListRequest request, String domainId, int maxPages) {
        if (maxPages <= 0) {
            throw new IllegalArgumentException("maxPages must be > 0");
        }
        DnsListRequest seed = request == null ? DnsListRequest.create() : request;
        int page = seed.getPage() == null || seed.getPage() < 1 ? 1 : seed.getPage();
        int pageSize = seed.getPageSize() == null || seed.getPageSize() < 1 ? 50 : seed.getPageSize();

        List<DomainDNSRecord> all = new ArrayList<>();
        ApiResponse<List<DomainDNSRecord>> lastPage = null;
        int collectedPages = 0;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("Pagination interrupted");
            }
            if (collectedPages >= maxPages) {
                break;
            }
            DnsListRequest current = DnsListRequest.create()
                    .query(seed.getQuery())
                    .page(page)
                    .pageSize(pageSize);
            ApiResponse<List<DomainDNSRecord>> result = listWithInfo(current, domainId);
            lastPage = result;
            all.addAll(result.getResult() == null ? List.of() : result.getResult());
            collectedPages++;

            ResultInfo info = result.getResultInfo();
            if (info == null || info.getTotalCount() == null || info.getPageSize() == null || info.getPage() == null) {
                break;
            }
            int totalPages = (int) Math.ceil((double) info.getTotalCount() / Math.max(1, info.getPageSize()));
            if (info.getPage() >= totalPages) {
                break;
            }
            page++;
        }
        if (lastPage == null) {
            return new ApiResponse<>(all, null, null, null);
        }
        return new ApiResponse<>(all, lastPage.getResultInfo(), lastPage.getMeta(), lastPage.getStatus());
    }
}

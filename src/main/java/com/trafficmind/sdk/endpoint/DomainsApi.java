package com.trafficmind.sdk.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.CreateDomainRequest;
import com.trafficmind.sdk.dto.ResponseDomainRecord;
import com.trafficmind.sdk.dto.ResponseDomainId;
import com.trafficmind.sdk.dto.ResultInfo;
import com.trafficmind.sdk.request.DomainListRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Domains endpoint wrapper. */
public final class DomainsApi {
    private final TrafficmindClient client;

    public DomainsApi(TrafficmindClient client) {
        this.client = client;
    }

    /** Lists domains. */
    public ApiResponse<List<ResponseDomainRecord>> list(DomainListRequest request) {
        Map<String, String> q = request == null ? Map.of() : request.toQueryParams();
        JsonNode root = client.getRaw("domains", q);
        return client.readApiResponseList(root, ResponseDomainRecord.class);
    }

    /** Lists domains plus pagination metadata (pagination when available). */
    public ApiResponse<List<ResponseDomainRecord>> listWithInfo(DomainListRequest request) {
        return list(request);
    }

    /** Create domain. */
    public ApiResponse<ResponseDomainRecord> create(CreateDomainRequest request) {
        JsonNode root = client.postRaw("domains", request);
        return client.readApiResponse(root, ResponseDomainRecord.class);
    }

    /** Domain details. */
    public ApiResponse<ResponseDomainRecord> get(String domainId) {
        JsonNode root = client.getRaw(client.path("domains", domainId), Map.of());
        return client.readApiResponse(root, ResponseDomainRecord.class);
    }

    /** Delete domain. */
    public ApiResponse<ResponseDomainId> delete(String domainId) {
        JsonNode root = client.deleteRaw(client.path("domains", domainId));
        return client.readApiResponse(root, ResponseDomainId.class);
    }

    /**
     * Returns a lazy stream that requests pages on demand.
     * The stream should be consumed sequentially.
     */
    public Stream<ApiResponse<ResponseDomainRecord>> streamAll(DomainListRequest request) {
        return streamAll(request, Integer.MAX_VALUE);
    }

    /**
     * Returns a lazy stream that requests pages on demand up to maxPages.
     * The stream should be consumed sequentially.
     */
    public Stream<ApiResponse<ResponseDomainRecord>> streamAll(DomainListRequest request, int maxPages) {
        if (maxPages <= 0) {
            throw new IllegalArgumentException("maxPages must be > 0");
        }
        final DomainListRequest seed = request == null ? DomainListRequest.create() : request;
        final int startPage = seed.getPage() == null || seed.getPage() < 1 ? 1 : seed.getPage();
        final int pageSize = seed.getPageSize() == null || seed.getPageSize() < 1 ? 50 : seed.getPageSize();

        Iterator<ApiResponse<ResponseDomainRecord>> iterator = new Iterator<>() {
            private int page = startPage;
            private int collectedPages = 0;
            private List<ResponseDomainRecord> currentItems = List.of();
            private ApiResponse<List<ResponseDomainRecord>> currentPageResponse;
            private int currentIndex = 0;
            private boolean done;

            @Override
            public boolean hasNext() {
                fetchPageIfNeeded();
                return currentIndex < currentItems.size();
            }

            @Override
            public ApiResponse<ResponseDomainRecord> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                ResponseDomainRecord item = currentItems.get(currentIndex++);
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
                DomainListRequest current = DomainListRequest.create()
                        .matchMode(seed.getMatchMode())
                        .query(seed.getQuery())
                        .page(page)
                        .pageSize(pageSize);
                currentPageResponse = listWithInfo(current);
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

    /** Iterates over all pages and returns the full domain list. */
    public ApiResponse<List<ResponseDomainRecord>> listAll(DomainListRequest request) {
        return listAll(request, Integer.MAX_VALUE);
    }

    /** Iterates over pages up to maxPages and returns collected domains. */
    public ApiResponse<List<ResponseDomainRecord>> listAll(DomainListRequest request, int maxPages) {
        if (maxPages <= 0) {
            throw new IllegalArgumentException("maxPages must be > 0");
        }
        DomainListRequest seed = request == null ? DomainListRequest.create() : request;
        int page = seed.getPage() == null || seed.getPage() < 1 ? 1 : seed.getPage();
        int pageSize = seed.getPageSize() == null || seed.getPageSize() < 1 ? 50 : seed.getPageSize();

        List<ResponseDomainRecord> all = new ArrayList<>();
        ApiResponse<List<ResponseDomainRecord>> lastPage = null;
        int collectedPages = 0;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("Pagination interrupted");
            }
            if (collectedPages >= maxPages) {
                break;
            }
            DomainListRequest current = DomainListRequest.create()
                    .matchMode(seed.getMatchMode())
                    .query(seed.getQuery())
                    .page(page)
                    .pageSize(pageSize);
            ApiResponse<List<ResponseDomainRecord>> result = listWithInfo(current);
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

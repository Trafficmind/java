package com.trafficmind.sdk.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.trafficmind.sdk.TrafficmindClient;
import com.trafficmind.sdk.dto.ApiResponse;
import com.trafficmind.sdk.dto.CDNStorageResponse;
import com.trafficmind.sdk.dto.CDNUserResponse;
import com.trafficmind.sdk.dto.CreateCDNStorageRequest;
import com.trafficmind.sdk.dto.CreateCDNUserRequest;
import com.trafficmind.sdk.dto.SuccessResponse;
import com.trafficmind.sdk.dto.SyncStatus;
import com.trafficmind.sdk.request.CdnStorageListRequest;

import java.util.List;
import java.util.Map;

/** CDN endpoint wrapper. */
public final class CdnApi {
    private final TrafficmindClient client;

    public CdnApi(TrafficmindClient client) {
        this.client = client;
    }

    /** Provide full list of CDN storages which is accessible for you. */
    public ApiResponse<List<CDNStorageResponse>> listStorages() {
        return listStorages(null);
    }

    /** Provide full list of CDN storages which is accessible for you. */
    public ApiResponse<List<CDNStorageResponse>> listStorages(CdnStorageListRequest request) {
        Map<String, String> q = request == null ? Map.of() : request.toQueryParams();
        JsonNode root = client.getRaw("cdn/storage", q);
        return client.readApiResponseList(root, CDNStorageResponse.class);
    }

    /** Provide list of CDN storages plus pagination metadata (pagination when available). */
    public ApiResponse<List<CDNStorageResponse>> listStoragesWithInfo(CdnStorageListRequest request) {
        return listStorages(request);
    }

    /** Create new CDN storage where you can store your files. */
    public ApiResponse<CDNStorageResponse> createStorage(CreateCDNStorageRequest request) {
        JsonNode root = client.postRaw("cdn/storage", request);
        return client.readApiResponse(root, CDNStorageResponse.class);
    }

    /** Delete your CDN storage. */
    public ApiResponse<SuccessResponse> deleteStorage(String storageId) {
        JsonNode root = client.deleteRaw(client.path("cdn", "storage", storageId));
        return client.readApiResponse(root, SuccessResponse.class);
    }

    /** Refresh CDN storage from remote server. */
    public ApiResponse<SyncStatus> refreshStorage(String storageId) {
        JsonNode root = client.postRaw(client.path("cdn", "storage", storageId, "refresh"), null);
        return client.readApiResponse(root, SyncStatus.class);
    }

    /** Provide sftp credentials for connection. */
    public ApiResponse<CDNUserResponse> getSftpCredentials(String storageId) {
        JsonNode root = client.getRaw(client.path("cdn", "storage", storageId, "user"), Map.of());
        return client.readApiResponse(root, CDNUserResponse.class);
    }

    /** Create new sftp credentials if they don't exist. */
    public ApiResponse<CDNUserResponse> createSftpUser(CreateCDNUserRequest request) {
        JsonNode root = client.postRaw("cdn/user", request);
        return client.readApiResponse(root, CDNUserResponse.class);
    }

    /** Convenience overload — creates sftp credentials for the given storage ID. */
    public ApiResponse<CDNUserResponse> createSftpUser(String storageId) {
        CreateCDNUserRequest req = new CreateCDNUserRequest();
        req.setStorageId(storageId);
        return createSftpUser(req);
    }

    /** Revoke old sftp credentials and simultaneously provides new credentials. */
    public ApiResponse<CDNUserResponse> revokeSftpUser(String username) {
        JsonNode root = client.postRaw(client.path("cdn", "user", username, "revoke"), null);
        return client.readApiResponse(root, CDNUserResponse.class);
    }
}

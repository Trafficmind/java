package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CDNStorageResponse {

    @JsonProperty("bytes_total")
    private Integer bytesTotal;

    @JsonProperty("cdn_user")
    private CDNUserResponse cdnUser;

    @JsonProperty("deleted_at")
    private String deletedAt;

    @JsonProperty("files_count")
    private Integer filesCount;

    @JsonProperty("id")
    private String id;

    @JsonProperty("last_file_change_at")
    private String lastFileChangeAt;

    @JsonProperty("last_refresh_at")
    private String lastRefreshAt;

    @JsonProperty("name")
    private String name;

    @JsonProperty("needs_refresh")
    private Boolean needsRefresh;

    @JsonProperty("paths")
    private List<CDNStoragePathResponse> paths;

    @JsonProperty("purge_at")
    private String purgeAt;

    @JsonProperty("start_refresh_at")
    private String startRefreshAt;

    @JsonProperty("synced_dc")
    private List<RefreshedCDN> syncedDc;

    public CDNStorageResponse() {}

    public Integer getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(Integer bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public CDNUserResponse getCdnUser() {
        return cdnUser;
    }

    public void setCdnUser(CDNUserResponse cdnUser) {
        this.cdnUser = cdnUser;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Integer getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(Integer filesCount) {
        this.filesCount = filesCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastFileChangeAt() {
        return lastFileChangeAt;
    }

    public void setLastFileChangeAt(String lastFileChangeAt) {
        this.lastFileChangeAt = lastFileChangeAt;
    }

    public String getLastRefreshAt() {
        return lastRefreshAt;
    }

    public void setLastRefreshAt(String lastRefreshAt) {
        this.lastRefreshAt = lastRefreshAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getNeedsRefresh() {
        return needsRefresh;
    }

    public void setNeedsRefresh(Boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }

    public List<CDNStoragePathResponse> getPaths() {
        return paths;
    }

    public void setPaths(List<CDNStoragePathResponse> paths) {
        this.paths = paths;
    }

    public String getPurgeAt() {
        return purgeAt;
    }

    public void setPurgeAt(String purgeAt) {
        this.purgeAt = purgeAt;
    }

    public String getStartRefreshAt() {
        return startRefreshAt;
    }

    public void setStartRefreshAt(String startRefreshAt) {
        this.startRefreshAt = startRefreshAt;
    }

    public List<RefreshedCDN> getSyncedDc() {
        return syncedDc;
    }

    public void setSyncedDc(List<RefreshedCDN> syncedDc) {
        this.syncedDc = syncedDc;
    }

    @Override
    public String toString() {
        return "CDNStorageResponse{" + "bytesTotal=" + bytesTotal + ", " + "cdnUser=" + cdnUser + ", " + "deletedAt=" + deletedAt + ", " + "filesCount=" + filesCount + ", " + "id=" + id + ", " + "lastFileChangeAt=" + lastFileChangeAt + ", " + "lastRefreshAt=" + lastRefreshAt + ", " + "name=" + name + ", " + "needsRefresh=" + needsRefresh + ", " + "paths=" + paths + ", " + "purgeAt=" + purgeAt + ", " + "startRefreshAt=" + startRefreshAt + ", " + "syncedDc=" + syncedDc + "}";
    }
}
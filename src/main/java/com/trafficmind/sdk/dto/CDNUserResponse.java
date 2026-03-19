package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CDNUserResponse {

    @JsonProperty("password")
    private String password;

    @JsonProperty("sftp_host")
    private String sftpHost;

    @JsonProperty("sftp_port")
    private Integer sftpPort;

    @JsonProperty("storage_id")
    private String storageId;

    @JsonProperty("username")
    private String username;

    public CDNUserResponse() {}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSftpHost() {
        return sftpHost;
    }

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public Integer getSftpPort() {
        return sftpPort;
    }

    public void setSftpPort(Integer sftpPort) {
        this.sftpPort = sftpPort;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "CDNUserResponse{" + "password=[REDACTED], " + "sftpHost=" + sftpHost + ", " + "sftpPort=" + sftpPort + ", " + "storageId=" + storageId + ", " + "username=" + username + "}";
    }
}

package com.trafficmind.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateDomainRequest {

    @JsonProperty("dns_file_content")
    private String dnsFileContent;

    @JsonProperty("dns_method")
    private DnsMethod dnsMethod;

    @JsonProperty("group_id")
    private Integer groupId;

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("source_domain_id")
    private String sourceDomainId;

    public CreateDomainRequest() {}

    @JsonIgnore
    public List<Integer> getDnsFileContent() {
        if (dnsFileContent == null) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(dnsFileContent);
        List<Integer> values = new ArrayList<>(decoded.length);
        for (byte value : decoded) {
            values.add(Byte.toUnsignedInt(value));
        }
        return values;
    }

    public String getDnsFileContentBase64() {
        return dnsFileContent;
    }

    @JsonIgnore
    public void setDnsFileContent(List<Integer> dnsFileContent) {
        if (dnsFileContent == null) {
            setDnsFileContentBase64(null);
            return;
        }
        byte[] bytes = new byte[dnsFileContent.size()];
        for (int i = 0; i < dnsFileContent.size(); i++) {
            bytes[i] = (byte) (dnsFileContent.get(i) == null ? 0 : dnsFileContent.get(i).intValue());
        }
        this.dnsFileContent = Base64.getEncoder().encodeToString(bytes);
    }

    public void setDnsFileContentBase64(String dnsFileContent) {
        this.dnsFileContent = dnsFileContent;
    }

    public String getDnsMethod() {
        return dnsMethod == null ? null : dnsMethod.getValue();
    }

    public void setDnsMethod(String dnsMethod) {
        this.dnsMethod = DnsMethod.fromValue(dnsMethod);
    }

    public DnsMethod getDnsMethodEnum() {
        return dnsMethod;
    }

    public void setDnsMethodEnum(DnsMethod dnsMethod) {
        this.dnsMethod = dnsMethod;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceDomainId() {
        return sourceDomainId;
    }

    public void setSourceDomainId(String sourceDomainId) {
        this.sourceDomainId = sourceDomainId;
    }

    @Override
    public String toString() {
        return "CreateDomainRequest{" + "dnsFileContent=" + dnsFileContent + ", " + "dnsMethod=" + dnsMethod + ", " + "groupId=" + groupId + ", " + "name=" + name + ", " + "sourceDomainId=" + sourceDomainId + "}";
    }
}

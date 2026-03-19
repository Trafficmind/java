package com.trafficmind.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

final class TrafficmindClientResultExtractionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void extractResultNodeReadsPayloadRecords() throws Exception {
        assertExtracted("{\"payload\":{\"records\":[{\"id\":\"r1\"}]}}", extracted -> extracted.get(0).path("id").asText(), "r1");
    }

    @Test
    void extractResultNodeReadsPayloadItems() throws Exception {
        assertExtracted("{\"payload\":{\"items\":[{\"id\":\"i1\"}]}}", extracted -> extracted.get(0).path("id").asText(), "i1");
    }

    @Test
    void extractResultNodeReadsPayloadBatch() throws Exception {
        assertExtracted("{\"payload\":{\"batch\":{\"posts\":[{\"id\":\"b1\"}]}}}", extracted -> extracted.path("posts").get(0).path("id").asText(), "b1");
    }

    @Test
    void extractResultNodeReadsPayloadResult() throws Exception {
        assertExtracted("{\"payload\":{\"result\":{\"message\":\"ok\"}}}", extracted -> extracted.path("message").asText(), "ok");
    }

    @Test
    void extractResultNodeReadsPayloadDomain() throws Exception {
        assertExtracted("{\"payload\":{\"domain\":{\"id\":\"d1\"}}}", extracted -> extracted.path("id").asText(), "d1");
    }

    @Test
    void extractResultNodeReadsPayloadStorage() throws Exception {
        assertExtracted("{\"payload\":{\"storage\":{\"id\":\"s1\"}}}", extracted -> extracted.path("id").asText(), "s1");
    }

    @Test
    void extractResultNodeReadsPayloadUser() throws Exception {
        assertExtracted("{\"payload\":{\"user\":{\"username\":\"u1\"}}}", extracted -> extracted.path("username").asText(), "u1");
    }

    @Test
    void extractResultNodeReadsPayloadSetting() throws Exception {
        assertExtracted("{\"payload\":{\"setting\":{\"id\":\"ssl\"}}}", extracted -> extracted.path("id").asText(), "ssl");
    }

    @Test
    void extractResultNodeReadsPayloadSync() throws Exception {
        assertExtracted("{\"payload\":{\"sync\":{\"status\":\"queued\"}}}", extracted -> extracted.path("status").asText(), "queued");
    }

    @Test
    void extractResultNodeReturnsMissingNodeWhenPayloadAbsent() throws Exception {
        TrafficmindClient client = client();
        JsonNode root = MAPPER.readTree("{\"meta\":{\"request_id\":\"r\"}}");
        JsonNode extracted = client.extractResultNode(root);
        assertInstanceOf(MissingNode.class, extracted);
        assertSame(MissingNode.getInstance(), extracted);
    }

    private static void assertExtracted(String json, JsonExtractor extractor, String expected) throws Exception {
        TrafficmindClient client = client();
        JsonNode root = MAPPER.readTree(json);
        JsonNode extracted = client.extractResultNode(root);
        assertEquals(expected, extractor.read(extracted));
    }

    private static TrafficmindClient client() {
        return TrafficmindClient.builder()
                .email("e")
                .apiKey("k")
                .baseUrl("http://localhost")
                .allowInsecureTransportForTesting(true)
                .build();
    }

    @FunctionalInterface
    private interface JsonExtractor {
        String read(JsonNode node);
    }
}

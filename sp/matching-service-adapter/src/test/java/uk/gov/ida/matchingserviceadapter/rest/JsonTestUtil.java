package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class JsonTestUtil {

    public static String jsonFixture(ObjectMapper objectMapper,  String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(new String(JsonTestUtil.class.getClassLoader().getResourceAsStream(filename).readAllBytes(), StandardCharsets.UTF_8), JsonNode.class));
    }

}

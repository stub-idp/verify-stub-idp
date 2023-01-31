package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchingServiceResponseDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = Jackson.newObjectMapper().setDateFormat(StdDateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws Exception {
        String jsonString = objectMapper.writeValueAsString(MatchingServiceResponseDto.MATCH_RESPONSE);

        assertThat(jsonString).isEqualTo(jsonFixture("matching-service-response.json"));
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        MatchingServiceResponseDto deserializedValue =
                objectMapper.readValue(jsonFixture("matching-service-response.json"), MatchingServiceResponseDto.class);

        MatchingServiceResponseDto expectedValue = MatchingServiceResponseDto.MATCH_RESPONSE;
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private String jsonFixture(String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(new String(getClass().getClassLoader().getResourceAsStream(filename).readAllBytes(), StandardCharsets.UTF_8), JsonNode.class));
    }
}

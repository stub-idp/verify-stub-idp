package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.ida.matchingserviceadapter.builders.AddressDtoBuilder;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueDtoBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.TransliterableMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.VerifyMatchingDatasetDtoBuilder.aVerifyMatchingDatasetDto;

//
// These tests exist to prevent accidentally breaking our contract with the matching service. If they fail, ensure you
// are making changes in such a way that will not break our contract (i.e. use the expand/contract pattern); don't
// simply fix the test.
//
public class AssertionServiceRequestDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper = Jackson.newObjectMapper().setDateFormat(StdDateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws IOException {
        VerifyMatchingServiceRequestDto originalDto = getMatchingServiceRequestDto();

        String jsonString = objectMapper.writeValueAsString(originalDto);
        VerifyMatchingServiceRequestDto reserializedDto = objectMapper.readValue(jsonString, VerifyMatchingServiceRequestDto.class);

        assertThat(reserializedDto).isEqualTo(originalDto);
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        VerifyMatchingServiceRequestDto deserializedValue =
                objectMapper.readValue(jsonFixture("matching-service-request.json"), VerifyMatchingServiceRequestDto.class);

        VerifyMatchingServiceRequestDto expectedValue = getMatchingServiceRequestDto();
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private VerifyMatchingServiceRequestDto getMatchingServiceRequestDto() {
        LevelOfAssuranceDto levelOfAssurance = LevelOfAssuranceDto.LEVEL_1;
        VerifyMatchingDatasetDto matchingDataset = getVerifyMatchingDataset(LocalDate.parse("2014-02-01"));
        Cycle3DatasetDto cycle3DatasetDto = Cycle3DatasetDto.createFromData(ImmutableMap.of("NI", "1234"));
        String hashedPid = "8f2f8c23-f767-4590-aee9-0842f7f1e36d";
        String matchId = "cda6126c-9695-4051-ba6f-27a8938a0b03";
        return new VerifyMatchingServiceRequestDto(
                matchingDataset,
                Optional.of(cycle3DatasetDto),
                hashedPid,
                matchId,
                levelOfAssurance);
    }

    private VerifyMatchingDatasetDto getVerifyMatchingDataset(LocalDate dateTime) {
        return (VerifyMatchingDatasetDto) aVerifyMatchingDatasetDto()
                .addSurname(getTransliterableMdsValue("walker", null, dateTime))
                .withAddressHistory(ImmutableList.of(getAddressDto("EC2", dateTime), getAddressDto("WC1", dateTime)))
                .withDateOfBirth(getSimpleMdsValue(dateTime, dateTime))
                .withFirstname(getTransliterableMdsValue("walker", null, dateTime))
                .withGender(getSimpleMdsValue(GenderDto.FEMALE, dateTime))
                .withMiddleNames(getSimpleMdsValue("walker", dateTime))
                .withSurnameHistory(
                        ImmutableList.of(
                                getTransliterableMdsValue("smith", null, dateTime),
                                getTransliterableMdsValue("walker", null, dateTime)
                        ))
                .build();
    }

    private String jsonFixture(String filename) throws IOException {
        return objectMapper.writeValueAsString(objectMapper.readValue(new String(getClass().getClassLoader().getResourceAsStream(filename).readAllBytes(), StandardCharsets.UTF_8), JsonNode.class));
    }

    private VerifyAddressDto getAddressDto(String postcode, LocalDate dateTime) {
        return new AddressDtoBuilder()
                .withFromDate(dateTime)
                .withInternationalPostCode("123")
                .withLines(ImmutableList.of("a", "b")).withPostCode(postcode)
                .withToDate(dateTime)
                .withUPRN("urpn")
                .withVerified(true)
                .buildVerifyAddressDto();
    }

    private <T> SimpleMdsValueDto<T> getSimpleMdsValue(T value, LocalDate dateTime) {
        return new SimpleMdsValueDtoBuilder<T>()
                .withFrom(dateTime)
                .withTo(dateTime)
                .withValue(value)
                .withVerifiedStatus(true)
                .build();
    }

    private TransliterableMdsValueDto getTransliterableMdsValue(String value, String nonLatinScriptValue, LocalDate dateTime) {
        return new TransliterableMdsValueDto(value, nonLatinScriptValue, dateTime, dateTime, true);
    }
}

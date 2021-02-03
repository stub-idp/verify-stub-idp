package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.ida.matchingserviceadapter.builders.AddressDtoBuilder;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueDtoBuilder;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.VerifyMatchingDatasetDtoBuilder.aVerifyMatchingDatasetDto;
import static uk.gov.ida.matchingserviceadapter.rest.JsonTestUtil.jsonFixture;

public class VerifyMatchingDatasetDtoTest {

    private ObjectMapper objectMapper;
    private static final LocalDate date = LocalDate.parse("2014-02-01");

    @BeforeEach
    public void setUp() {
        objectMapper = Jackson.newObjectMapper().setDateFormat(StdDateFormat.getDateInstance());
    }

    @Test
    public void shouldSerializeToJson() throws IOException {

        MatchingDatasetDto originalDto = createVerifyMatchingDatasetDto(date);

        String serializedJson = objectMapper.writeValueAsString(originalDto);
        MatchingDatasetDto reserializedDto = objectMapper.readValue(serializedJson, VerifyMatchingDatasetDto.class);

        assertThat(reserializedDto).isEqualTo(originalDto);
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        VerifyMatchingDatasetDto deserializedValue =
                objectMapper.readValue(jsonFixture(objectMapper, "verify-matching-dataset.json"), VerifyMatchingDatasetDto.class);

        MatchingDatasetDto expectedValue = createVerifyMatchingDatasetDto(date);
        assertThat(deserializedValue).isEqualTo(expectedValue);
    }

    private MatchingDatasetDto createVerifyMatchingDatasetDto(LocalDate dateTime) {
        return aVerifyMatchingDatasetDto()
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

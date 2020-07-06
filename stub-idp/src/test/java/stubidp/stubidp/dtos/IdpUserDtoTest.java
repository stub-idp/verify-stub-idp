package stubidp.stubidp.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.test.AddressBuilder;
import stubidp.stubidp.builders.SimpleMdsValueBuilder;
import stubidp.stubidp.domain.MatchingDatasetValue;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.stubidp.builders.IdpUserDtoBuilder.anIdpUserDto;

public class IdpUserDtoTest {
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @Disabled(value = "validates data that has been serialized with older code can still be read")
    public void shouldDeSerialiseJsonToObjectWhenAllFieldsArePopulatedOldFormat() throws IOException {
        IdpUserDto idpUserDtoFromJson = objectMapper.readValue("{\"pid\":\"00754148-902f-4d94-b0db-cb1f7eb3fd84\",\"username\":\"user1\",\"password\":\"$2a$10$Dn3LpR9//dOJlnaHCMYMLutaWtEOphNbak9/jAWjaEc3Lt0H9jQy.\",\"firstName\":{\"value\":\"Fred\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"middleNames\":{\"value\":\"Flintstone\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"gender\":{\"value\":\"MALE\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"dateOfBirth\":{\"value\":[1970,1,1],\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"address\":{\"verified\":false,\"from\":978307200000,\"to\":1355270400000,\"postCode\":\"WC2B 6NH\",\"lines\":[\"Aviation House\",\"London\"],\"internationalPostCode\":null,\"uprn\":null},\"levelOfAssurance\":\"LEVEL_2\",\"surnames\":[{\"value\":\"Smith\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},{\"value\":\"Henry\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true}]}", IdpUserDto.class);
        IdpUserDto idpuserDto = generateTestUserDto();
        assertThat(compareIdpUserDto(idpUserDtoFromJson, idpuserDto)).isTrue();
    }

    @Test
    public void shouldDeSerialiseJsonToObjectWhenAllFieldsArePopulated() throws IOException {
        // NOTE: this is a change to the previous serialized data format
        IdpUserDto idpUserDtoFromJson = objectMapper.readValue("{\"pid\":\"00754148-902f-4d94-b0db-cb1f7eb3fd84\",\"username\":\"user1\",\"password\":\"$2a$10$Dn3LpR9//dOJlnaHCMYMLutaWtEOphNbak9/jAWjaEc3Lt0H9jQy.\",\"firstName\":{\"value\":\"Fred\",\"from\":315532800.000,\"to\":1356998400.000,\"verified\":true},\"middleNames\":{\"value\":\"Flintstone\",\"from\":315532800.000,\"to\":1356998400.000,\"verified\":true},\"gender\":{\"value\":\"MALE\",\"from\":315532800.000,\"to\":1356998400.000,\"verified\":true},\"dateOfBirth\":{\"value\":0,\"from\":315532800.000,\"to\":1356998400.000,\"verified\":true},\"address\":{\"verified\":false,\"from\":978307200.000,\"to\":1355270400.000,\"postCode\":\"WC2B 6NH\",\"lines\":[\"Aviation House\",\"London\"],\"internationalPostCode\":null,\"uprn\":null},\"levelOfAssurance\":\"LEVEL_2\",\"surnames\":[{\"value\":\"Smith\",\"from\":315532800.000,\"to\":1356998400.000,\"verified\":true},{\"value\":\"Henry\",\"from\":315532800.000,\"to\":1356998400.000,\"verified\":true}]}", IdpUserDto.class);
        IdpUserDto idpuserDto = generateTestUserDto();
        assertThat(compareIdpUserDto(idpUserDtoFromJson, idpuserDto)).isTrue();
    }

    private boolean compareIdpUserDto(final IdpUserDto idpUserDtoFromJson, final IdpUserDto idpuserDto) {
        assertThat(idpuserDto.getPid()).isEqualTo(idpUserDtoFromJson.getPid());
        assertThat(idpuserDto.getUsername()).isEqualTo(idpUserDtoFromJson.getUsername());
        assertThat(idpuserDto.getPassword()).isEqualTo(idpUserDtoFromJson.getPassword());

        assertThat(idpuserDto.getFirstName().isPresent()).isTrue();
        assertThat(idpUserDtoFromJson.getFirstName().isPresent()).isTrue();
        MatchingDatasetValue<String> idpuserDtoFirstname = idpuserDto.getFirstName().get();
        MatchingDatasetValue<String> idpUserDtoFromJsonFirstname = idpUserDtoFromJson.getFirstName().get();
        compareSimpleMdsObjects(idpuserDtoFirstname,idpUserDtoFromJsonFirstname);

        assertThat(idpuserDto.getMiddleNames().isPresent()).isTrue();
        assertThat(idpUserDtoFromJson.getMiddleNames().isPresent()).isTrue();
        MatchingDatasetValue<String> idpUserDtoMiddleNames = idpuserDto.getMiddleNames().get();
        MatchingDatasetValue<String> idpUserDtoFromJsonMiddleNames = idpUserDtoFromJson.getMiddleNames().get();
        compareSimpleMdsObjects(idpUserDtoMiddleNames,idpUserDtoFromJsonMiddleNames);

        assertThat(idpuserDto.getSurnames().size()).isEqualTo(2);
        assertThat(idpUserDtoFromJson.getSurnames().size()).isEqualTo(2);
        MatchingDatasetValue<String> idpUserDtoFirstSurname = idpuserDto.getSurnames().get(0);
        MatchingDatasetValue<String> idpUserDtoFromJsonFirstSurname = idpUserDtoFromJson.getSurnames().get(0);
        compareSimpleMdsObjects(idpUserDtoFirstSurname,idpUserDtoFromJsonFirstSurname);

        MatchingDatasetValue<String> idpUserDtoSecondSurname = idpuserDto.getSurnames().get(1);
        MatchingDatasetValue<String> idpUserFromJsonDtoSecondSurname = idpUserDtoFromJson.getSurnames().get(1);
        compareSimpleMdsObjects(idpUserDtoSecondSurname,idpUserFromJsonDtoSecondSurname);

        assertThat(idpuserDto.getGender().isPresent()).isTrue();
        assertThat(idpUserDtoFromJson.getGender().isPresent()).isTrue();
        MatchingDatasetValue<Gender> idpuserDtoGender = idpuserDto.getGender().get();
        MatchingDatasetValue<Gender> idpUserDtoFromJsonGender = idpUserDtoFromJson.getGender().get();
        compareSimpleMdsObjects(idpuserDtoGender,idpUserDtoFromJsonGender);

        assertThat(idpuserDto.getDateOfBirth().isPresent()).isTrue();
        assertThat(idpUserDtoFromJson.getDateOfBirth().isPresent()).isTrue();
        MatchingDatasetValue<Instant> idpuserDtoDateOfBirth = idpuserDto.getDateOfBirth().get();
        MatchingDatasetValue<Instant> idpUserDtoFromJsonDateOfBirth = idpUserDtoFromJson.getDateOfBirth().get();
        compareSimpleMdsObjects(idpuserDtoDateOfBirth, idpUserDtoFromJsonDateOfBirth);


        assertThat(idpuserDto.getAddress().isPresent()).isTrue();
        assertThat(idpUserDtoFromJson.getAddress().isPresent()).isTrue();
        Address idpuserDtoAddress = idpuserDto.getAddress().get();
        Address idpUserDtoFromJsonAddress = idpUserDtoFromJson.getAddress().get();
        assertThat(idpuserDtoAddress.getFrom().toString()).isEqualTo(idpUserDtoFromJsonAddress.getFrom().toString());
        assertThat(idpuserDtoAddress.getTo().toString()).isEqualTo(idpUserDtoFromJsonAddress.getTo().toString());
        assertThat(idpuserDtoAddress.getInternationalPostCode()).isEqualTo(idpUserDtoFromJsonAddress.getInternationalPostCode());
        assertThat(idpuserDtoAddress.getLines()).isEqualTo(idpUserDtoFromJsonAddress.getLines());
        assertThat(idpuserDtoAddress.getPostCode()).isEqualTo(idpUserDtoFromJsonAddress.getPostCode());
        assertThat(idpuserDtoAddress.getUPRN()).isEqualTo(idpUserDtoFromJsonAddress.getUPRN());
        assertThat(idpuserDtoAddress.isVerified()).isEqualTo(idpUserDtoFromJsonAddress.isVerified());

        assertThat(idpuserDto.getLevelOfAssurance()).isEqualTo(idpUserDtoFromJson.getLevelOfAssurance());

        return true;
    }

    private IdpUserDto generateTestUserDto() {
        return anIdpUserDto()
                .withPid("00754148-902f-4d94-b0db-cb1f7eb3fd84")
                .withUserName("user1")
                .withPassword("$2a$10$Dn3LpR9//dOJlnaHCMYMLutaWtEOphNbak9/jAWjaEc3Lt0H9jQy.")
                .withFirsName(createSimpleMdsValue("Fred"))
                .withMiddleNames(createSimpleMdsValue("Flintstone"))
                .addSurname(createSimpleMdsValue("Smith"))
                .addSurname(createSimpleMdsValue("Henry"))
                .withGender(
                        SimpleMdsValueBuilder.<Gender>aSimpleMdsValue()
                                .withValue(Gender.MALE)
                                .withFrom(LocalDateTime.of(LocalDate.of(1980, 1, 1), LocalTime.of(0, 0, 0)).atZone(ZoneId.of("UTC")).toInstant())
                                .withTo(LocalDateTime.of(LocalDate.of(2013, 1, 1), LocalTime.of(0, 0, 0)).atZone(ZoneId.of("UTC")).toInstant())
                                .withVerifiedStatus(true)
                                .build()
                )
                .withDateOfBirth(
                        SimpleMdsValueBuilder.<Instant>aSimpleMdsValue()
                                .withValue(getLocalDateTime(1970, 1, 1, 0, 0, 0))
                                .withFrom(getLocalDateTime(1980, 1, 1, 0, 0, 0))
                                .withTo(getLocalDateTime(2013, 1, 1, 0, 0, 0))
                                .withVerifiedStatus(true)
                                .build()
                )
                .withAddress(
                        AddressBuilder.anAddress()
                                .withFromDate(getLocalDateTime(2001, 1, 1, 0, 0, 0))
                                .withToDate(getLocalDateTime(2012, 12, 12, 0, 0, 0))
                                .withVerified(false)
                                .withPostCode("WC2B 6NH")
                                .withLines(
                                        asList(
                                                "Aviation House",
                                                "London"
                                        )
                                )
                                .build()
                )
                .withLevelOfAssurance("LEVEL_2")
                .build();
    }

    private <T> void compareSimpleMdsObjects(final MatchingDatasetValue<T> firstSimpleMdsValue, final MatchingDatasetValue<T> secondSimpleMdsValue) {
        assertThat(firstSimpleMdsValue.getValue()).isEqualTo(secondSimpleMdsValue.getValue());
        assertThat(firstSimpleMdsValue.getFrom()).isEqualTo(secondSimpleMdsValue.getFrom());
        assertThat(firstSimpleMdsValue.getTo()).isEqualTo(secondSimpleMdsValue.getTo());
        assertThat(firstSimpleMdsValue.isVerified()).isEqualTo(secondSimpleMdsValue.isVerified());
    }


    private MatchingDatasetValue<String> createSimpleMdsValue(String value) {
        return SimpleMdsValueBuilder.<String>aSimpleMdsValue()
                .withValue(value)
                .withFrom(getLocalDateTime(1980, 1, 1, 0, 0, 0))
                .withTo(getLocalDateTime(2013, 1, 1, 0, 0, 0))
                .withVerifiedStatus(true)
                .build();
    }

    private Instant getLocalDateTime(int year, int month, int day, int hour, int minute, int seconds) {
        return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, seconds)).atZone(ZoneId.of("UTC")).toInstant();
    }
}

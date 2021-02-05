package stubidp.stubidp.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.test.builders.AddressBuilder;
import stubidp.saml.test.builders.SimpleMdsValueBuilder;

import java.io.IOException;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.stubidp.builders.IdpUserDtoBuilder.anIdpUserDto;

public class IdpUserDtoTest {
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @Disabled(value = "validates data that has been serialized with older code can still be read")
    void shouldDeSerialiseJsonToObjectWhenAllFieldsArePopulatedOldFormat() throws IOException {
        IdpUserDto idpUserDtoFromJson = objectMapper.readValue("{\"pid\":\"00754148-902f-4d94-b0db-cb1f7eb3fd84\",\"username\":\"user1\",\"password\":\"$2a$10$Dn3LpR9//dOJlnaHCMYMLutaWtEOphNbak9/jAWjaEc3Lt0H9jQy.\",\"firstName\":{\"value\":\"Fred\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"middleNames\":{\"value\":\"Flintstone\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"gender\":{\"value\":\"MALE\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"dateOfBirth\":{\"value\":[1970,1,1],\"from\":315532800000,\"to\":1356998400000,\"verified\":true},\"address\":{\"verified\":false,\"from\":978307200000,\"to\":1355270400000,\"postCode\":\"WC2B 6NH\",\"lines\":[\"Aviation House\",\"London\"],\"internationalPostCode\":null,\"uprn\":null},\"levelOfAssurance\":\"LEVEL_2\",\"surnames\":[{\"value\":\"Smith\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true},{\"value\":\"Henry\",\"from\":315532800000,\"to\":1356998400000,\"verified\":true}]}", IdpUserDto.class);
        IdpUserDto idpuserDto = generateTestUserDto();
        assertThat(idpUserDtoFromJson).isEqualTo(idpuserDto);
    }

    @Test
    void shouldDeSerialiseJsonToObjectWhenAllFieldsArePopulated() throws IOException {
        // NOTE: this is a change to the previous serialized data format
        IdpUserDto idpUserDtoFromJson = objectMapper.readValue("{\"pid\":\"00754148-902f-4d94-b0db-cb1f7eb3fd84\",\"username\":\"user1\",\"password\":\"$2a$10$Dn3LpR9//dOJlnaHCMYMLutaWtEOphNbak9/jAWjaEc3Lt0H9jQy.\",\"firstName\":{\"value\":\"Fred\",\"from\":[1980,1,1],\"to\":[2013,1,1],\"verified\":true},\"middleNames\":{\"value\":\"Flintstone\",\"from\":[1980,1,1],\"to\":[2013,1,1],\"verified\":true},\"gender\":{\"value\":\"MALE\",\"from\":[1980,1,1],\"to\":[2013,1,1],\"verified\":true},\"dateOfBirth\":{\"value\":[1970,1,1],\"from\":[1980,1,1],\"to\":[2013,1,1],\"verified\":true},\"address\":{\"lines\":[\"Aviation House\",\"London\"],\"postCode\":\"WC2B 6NH\",\"internationalPostCode\":null,\"uprn\":null,\"from\":[2001,1,1],\"to\":[2012,12,12],\"verified\":false},\"levelOfAssurance\":\"LEVEL_2\",\"surnames\":[{\"value\":\"Smith\",\"from\":[1980,1,1],\"to\":[2013,1,1],\"verified\":true},{\"value\":\"Henry\",\"from\":[1980,1,1],\"to\":[2013,1,1],\"verified\":true}]}", IdpUserDto.class);
        IdpUserDto idpuserDto = generateTestUserDto();
        assertThat(idpUserDtoFromJson).isEqualTo(idpuserDto);
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
                                .withFrom(LocalDate.of(1980, 1, 1))
                                .withTo(LocalDate.of(2013, 1, 1))
                                .withVerifiedStatus(true)
                                .build()
                )
                .withDateOfBirth(
                        SimpleMdsValueBuilder.<LocalDate>aSimpleMdsValue()
                                .withValue(LocalDate.of(1970, 1, 1))
                                .withFrom(LocalDate.of(1980, 1, 1))
                                .withTo(LocalDate.of(2013, 1, 1))
                                .withVerifiedStatus(true)
                                .build()
                )
                .withAddress(
                        AddressBuilder.anAddress()
                                .withFromDate(LocalDate.of(2001, 1, 1))
                                .withToDate(LocalDate.of(2012, 12, 12))
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

    private <T> void compareSimpleMdsObjects(final SimpleMdsValue<T> firstSimpleMdsValue, final SimpleMdsValue<T> secondSimpleMdsValue) {
        assertThat(firstSimpleMdsValue.getValue()).isEqualTo(secondSimpleMdsValue.getValue());
        assertThat(firstSimpleMdsValue.getFrom()).isEqualTo(secondSimpleMdsValue.getFrom());
        assertThat(firstSimpleMdsValue.getTo()).isEqualTo(secondSimpleMdsValue.getTo());
        assertThat(firstSimpleMdsValue.isVerified()).isEqualTo(secondSimpleMdsValue.isVerified());
    }


    private SimpleMdsValue<String> createSimpleMdsValue(String value) {
        return SimpleMdsValueBuilder.<String>aSimpleMdsValue()
                .withValue(value)
                .withFrom(LocalDate.of(1980, 1, 1))
                .withTo(LocalDate.of(2013, 1, 1))
                .withVerifiedStatus(true)
                .build();
    }

}

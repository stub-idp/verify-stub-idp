package uk.gov.ida.matchingserviceadapter.mappers;

import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.MatchingDataset;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.TransliterableMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.MatchingDatasetBuilder.aMatchingDataset;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.aCurrentAddress;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.aHistoricalAddress;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_FROM_DATE;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_HISTORICAL_FROM_DATE;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_HISTORICAL_TO_DATE;

public class MatchingDatasetToMatchingDatasetDtoMapperTest {

    private final MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper = new MatchingDatasetToMatchingDatasetDtoMapper();

    @Test
    public void shouldMapToVerifyMatchingDatasetDto() {
        LocalDate dob = LocalDate.of(1970, 1, 2);
        LocalDate oldDob = LocalDate.of(1970, 2, 1);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").withVerifiedStatus(true).build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Bob").build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").withVerifiedStatus(true).build())
                .addSurname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Smith").build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aHistoricalSimpleMdsValue().withValue(oldDob).build())
                .withCurrentAddresses(Collections.singletonList(aCurrentAddress().withPostCode("AA12BB").build()))
                .withPreviousAddresses(Collections.singletonList(aHistoricalAddress().withPostCode("CC12DD").build()))
                .withGender(SimpleMdsValueBuilder.<Gender>aCurrentSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build())
                .build();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);

        // Check that only the current first name is included
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("Joe", null, DEFAULT_FROM_DATE, null, true));
        // Check entire surname history is included
        assertThat(matchingDatasetDto.getSurnames().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Bloggs", null, DEFAULT_FROM_DATE, null, true));
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Smith", null, DEFAULT_HISTORICAL_FROM_DATE, DEFAULT_HISTORICAL_TO_DATE, false));
        // Check that only the current date of birth is included
        assertThat(matchingDatasetDto.getDateOfBirth()).contains(new SimpleMdsValueDto<>(dob, DEFAULT_FROM_DATE, null, false));
        // Check entire address history is included
        assertThat(matchingDatasetDto.getAddresses().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getAddresses().stream().filter(a -> a.getPostCode().equals(Optional.of("AA12BB"))).findFirst()).isPresent();
        assertThat(matchingDatasetDto.getAddresses().stream().filter(a -> a.getPostCode().equals(Optional.of("CC12DD"))).findFirst()).isPresent();
        // Check that gender is included
        assertThat(matchingDatasetDto.getGender()).contains(new SimpleMdsValueDto<>(GenderDto.NOT_SPECIFIED, DEFAULT_FROM_DATE, null, false));
    }

    @Test
    public void shouldMapCurrentFirstName() {
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("historical unverified: expected fourth").build())
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("current unverified: expected second").build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("historical verified: expected third").withVerifiedStatus(true).build())
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("current verified: expected first").withVerifiedStatus(true).build())
                .build();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);

        // Check that only the current first name is included
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("current verified: expected first", null, DEFAULT_FROM_DATE, null, true));
    }

    @Test
    public void shouldMapToUniversalMatchingDatasetDto() {
        LocalDate dob = LocalDate.of(1970, 1, 2);
        LocalDate oldDob = LocalDate.of(1970, 2, 1);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Bob").build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").build())
                .addSurname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Smith").build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aHistoricalSimpleMdsValue().withValue(oldDob).build())
                .withCurrentAddresses(Collections.singletonList(aCurrentAddress().withPostCode("AA12BB").build()))
                .withPreviousAddresses(Collections.singletonList(aHistoricalAddress().withPostCode("CC12DD").build()))
                .withGender(SimpleMdsValueBuilder.<Gender>aCurrentSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build())
                .build();

        UniversalMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(matchingDataset);

        // Check that only the current first name is included
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("Joe", null, DEFAULT_FROM_DATE, null, false));
        // Check entire surname history is included
        assertThat(matchingDatasetDto.getSurnames().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Bloggs", null, DEFAULT_FROM_DATE, null, false));
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Smith", null, DEFAULT_HISTORICAL_FROM_DATE, DEFAULT_HISTORICAL_TO_DATE, false));
        // Check that only the current date of birth is included
        assertThat(matchingDatasetDto.getDateOfBirth()).contains(new SimpleMdsValueDto<>(dob, DEFAULT_FROM_DATE, null, false));
        // Check entire address history is included
        assertThat(matchingDatasetDto.getAddresses()).isPresent();
        assertThat(matchingDatasetDto.getAddresses().get().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getAddresses().get().stream().filter(a -> a.getPostCode().equals(Optional.of("AA12BB"))).findFirst()).isPresent();
        assertThat(matchingDatasetDto.getAddresses().get().stream().filter(a -> a.getPostCode().equals(Optional.of("CC12DD"))).findFirst()).isPresent();
        // Check that gender is included
        assertThat(matchingDatasetDto.getGender()).contains(new SimpleMdsValueDto<>(GenderDto.NOT_SPECIFIED, DEFAULT_FROM_DATE, null, false));
    }

    @Test
    public void shouldMapToUniversalMatchingDatasetDtoWithTypicalEidasDataset() {
        LocalDate dob = LocalDate.of(1970, 1, 2);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").withFrom(null).withVerifiedStatus(true).build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").withFrom(null).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).withFrom(null).withVerifiedStatus(true).build())
                .build();

        UniversalMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(matchingDataset);

        // eIDAS matching datasets will never have a from or to date and will always be verified
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("Joe", null, null, null, true));
        assertThat(matchingDatasetDto.getSurnames()).containsOnly(new TransliterableMdsValueDto("Bloggs", null, null, null, true));
        assertThat(matchingDatasetDto.getDateOfBirth()).contains(new SimpleMdsValueDto<>(dob, null, null, true));
    }
}
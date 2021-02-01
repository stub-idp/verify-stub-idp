package uk.gov.ida.rp.testrp.builders;

import org.joda.time.LocalDate;
import uk.gov.ida.rp.testrp.contract.GenderDto;
import uk.gov.ida.rp.testrp.contract.SimpleMdsValueDto;
import uk.gov.ida.rp.testrp.contract.TransliterableMdsValueDto;
import uk.gov.ida.rp.testrp.contract.UniversalAddressDto;
import uk.gov.ida.rp.testrp.contract.UniversalMatchingDatasetDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class UniversalMatchingDatasetDtoBuilder {
    private List<UniversalAddressDto> addresses = new ArrayList<>();
    private SimpleMdsValueDto<LocalDate> dateOfBirth;
    private TransliterableMdsValueDto firstName;
    private TransliterableMdsValueDto middleNames;
    private List<TransliterableMdsValueDto> surnames = new ArrayList<>();
    private SimpleMdsValueDto<GenderDto> gender;

    public static UniversalMatchingDatasetDtoBuilder aUniversalMatchingDatasetDto() {
        return new UniversalMatchingDatasetDtoBuilder();
    }

    public UniversalMatchingDatasetDto build() {
        return new UniversalMatchingDatasetDto(addresses,
                dateOfBirth,
                firstName,
                middleNames,
                surnames,
                gender);
    }

    public static UniversalMatchingDatasetDto theSpecialCycle3UserMatchingDataset() {
        return aUniversalMatchingDatasetDto()
                .withFirstName(buildTransliterableMdsValue("J"))
                .withMiddleNames(null)
                .withSurnames(
                        asList(
                                buildTransliterableMdsValue("Moriarti"),
                                buildTransliterableMdsValue("Barnes")
                        )
                )
                .withGender(SimpleMdsValueDtoBuilder.<GenderDto>aSimpleMdsValueDto().withValue(GenderDto.NOT_SPECIFIED).build())
                .withDateOfBirth(
                        buildDateMdsValue("1822-11-27")
                )
                .withAddresses(
                        Collections.singletonList(
                                UniversalAddressDtoBuilder.aUniversalAddressDto()
                                        .withLines(Collections.singletonList("10 Two St"))
                                        .withPostCode("1A 2BC")
                                        .build()
                        )
                )
                .build();
    }

    public UniversalMatchingDatasetDtoBuilder withAddresses(List<UniversalAddressDto> addresses) {
        this.addresses = addresses;
        return this;
    }

    public UniversalMatchingDatasetDtoBuilder withDateOfBirth(SimpleMdsValueDto<LocalDate> dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public UniversalMatchingDatasetDtoBuilder withFirstName(TransliterableMdsValueDto firstName) {
        this.firstName = firstName;
        return this;
    }

    public UniversalMatchingDatasetDtoBuilder withMiddleNames(TransliterableMdsValueDto middleNames) {
        this.middleNames = middleNames;
        return this;
    }

    public UniversalMatchingDatasetDtoBuilder withSurnames(List surnames) {
        this.surnames = surnames;
        return this;
    }

    public UniversalMatchingDatasetDtoBuilder withGender(SimpleMdsValueDto gender) {
        this.gender = gender;
        return this;
    }

    private static TransliterableMdsValueDto buildTransliterableMdsValue(String value) {
        return new TransliterableMdsValueDto(value, null, null, null, true);
    }

    private static SimpleMdsValueDto<LocalDate> buildDateMdsValue(String date) {
        return SimpleMdsValueDtoBuilder.<LocalDate>aSimpleMdsValueDto()
                .withValue(LocalDate.parse(date))
                .build();
    }
}

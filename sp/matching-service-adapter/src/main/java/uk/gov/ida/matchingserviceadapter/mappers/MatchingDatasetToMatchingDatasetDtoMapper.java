package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import uk.gov.ida.matchingserviceadapter.ComparatorHelper;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.TransliterableMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToMatchingDatasetDtoMapper {

    public VerifyMatchingDatasetDto mapToVerifyMatchingDatasetDto(MatchingDataset matchingDataset) {
        Optional<TransliterableMdsValue> firstNameValue = matchingDataset.getFirstNames().stream()
                .min(ComparatorHelper.comparatorByVerifiedThenCurrent());
        Optional<SimpleMdsValue<String>> middleNameValue = matchingDataset.getMiddleNames().stream().findFirst();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = matchingDataset.getDateOfBirths().stream().findFirst();

        return new VerifyMatchingDatasetDto(
                firstNameValue.map(this::mapToMatchingDatasetTransliterableDto),
                middleNameValue.map(this::mapToMatchingDatasetDto),
                matchingDataset.getSurnames().stream().map(this::mapToMatchingDatasetTransliterableDto).collect(Collectors.toList()),
                matchingDataset.getGender().map(this::mapGender),
                birthDateValue.map(this::mapToMatchingDatasetDto),
                mapVerifyAddresses(matchingDataset.getAddresses()));
    }

    public UniversalMatchingDatasetDto mapToUniversalMatchingDatasetDto(MatchingDataset matchingDataset) {
        Optional<TransliterableMdsValue> firstNameValue = matchingDataset.getFirstNames().stream().findFirst();
        Optional<SimpleMdsValue<String>> middleNameValue = matchingDataset.getMiddleNames().stream().findFirst();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = matchingDataset.getDateOfBirths().stream().findFirst();

        return new UniversalMatchingDatasetDto(
                firstNameValue.map(this::mapToMatchingDatasetTransliterableDto),
                middleNameValue.map(this::mapToMatchingDatasetDto),
                matchingDataset.getSurnames().stream().map(this::mapToMatchingDatasetTransliterableDto).collect(Collectors.toList()),
                matchingDataset.getGender().map(this::mapGender),
                birthDateValue.map(this::mapToMatchingDatasetDto),
                Optional.ofNullable(mapToUniversalAddressDto(matchingDataset.getAddresses())));
    }

    private SimpleMdsValueDto<GenderDto> mapGender(SimpleMdsValue<Gender> simpleMdsValue) {
        return new SimpleMdsValueDto<>(convertToGenderDto(simpleMdsValue.getValue()),
                simpleMdsValue.getFrom(),
                simpleMdsValue.getTo(),
                simpleMdsValue.isVerified());
    }

    private GenderDto convertToGenderDto(Gender gender) {
        GenderDto genderDto;
        switch (gender) {
            case MALE:
                genderDto = GenderDto.MALE;
                break;
            case FEMALE:
                genderDto = GenderDto.FEMALE;
                break;
            case NOT_SPECIFIED:
                genderDto = GenderDto.NOT_SPECIFIED;
                break;
            default:
                throw new IllegalArgumentException("Illegal gender value: '" + gender + "'");
        }
        return genderDto;
    }

    private <T> SimpleMdsValueDto<T> mapToMatchingDatasetDto(SimpleMdsValue<T> simpleMdsValueOptional) {
        return new SimpleMdsValueDto<>(simpleMdsValueOptional.getValue(),
                simpleMdsValueOptional.getFrom(),
                simpleMdsValueOptional.getTo(),
                simpleMdsValueOptional.isVerified());
    }

    private TransliterableMdsValueDto mapToMatchingDatasetTransliterableDto(TransliterableMdsValue transliterableMdsValue) {
        return new TransliterableMdsValueDto(transliterableMdsValue.getValue(),
                transliterableMdsValue.getNonLatinScriptValue(),
                transliterableMdsValue.getFrom(),
                transliterableMdsValue.getTo(),
                transliterableMdsValue.isVerified());
    }

    private List<VerifyAddressDto> mapVerifyAddresses(List<Address> addresses) {
        return Lists.newArrayList(Collections2.transform(addresses, new Function<>() {
            @Nullable
            @Override
            public VerifyAddressDto apply(Address input) {
                return new VerifyAddressDto(input.getLines(), input.getPostCode(), input.getInternationalPostCode(), input.getUPRN(), input.getFrom(), Optional.ofNullable(input.getTo()), input.isVerified());
            }
        }));
    }

    private List<UniversalAddressDto> mapToUniversalAddressDto(List<Address> addresses) {
        return addresses
                .stream()
                .map(input -> new UniversalAddressDto(input.getLines(),
                        input.getPostCode(),
                        input.getInternationalPostCode(),
                        input.getUPRN(),
                        input.getFrom(),
                        Optional.ofNullable(input.getTo()),
                        input.isVerified()))
                .collect(Collectors.toList());
    }
}

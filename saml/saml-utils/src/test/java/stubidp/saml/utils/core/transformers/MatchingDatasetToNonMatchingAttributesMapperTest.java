package stubidp.saml.utils.core.transformers;

import org.junit.jupiter.api.Test;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import stubidp.saml.domain.matching.assertions.NonMatchingAddress;
import stubidp.saml.domain.matching.assertions.NonMatchingAttributes;
import stubidp.saml.domain.matching.assertions.NonMatchingTransliterableAttribute;
import stubidp.saml.domain.matching.assertions.NonMatchingVerifiableAttribute;
import stubidp.saml.test.verifyserviceProvider.dto.NonMatchingVerifiableAttributeBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class MatchingDatasetToNonMatchingAttributesMapperTest {

    private final MatchingDatasetToNonMatchingAttributesMapper matchingDatasetMapper = new MatchingDatasetToNonMatchingAttributesMapper();

    private final LocalDate fromOne = LocalDate.now();
    private final LocalDate fromTwo = LocalDate.now().minus(6, ChronoUnit.DAYS);
    private final LocalDate fromThree = LocalDate.now().minus(30, ChronoUnit.DAYS);
    private final LocalDate fromFour = null;

    private final String foo = "Foo";
    private final String bar = "Bar";
    private final String baz = "Baz";
    private final String fuu = "Fuu";

    @Test
    void shouldMapFirstNamesOrderedByFromDate() {
        final List<TransliterableMdsValue> firstNames = asList(
                createTransliterableValue(fromTwo, foo),
                createTransliterableValue(fromThree, bar),
                createTransliterableValue(fromOne, baz),
                createTransliterableValue(fromFour, fuu));

        final MatchingDataset matchingDataset = new MatchingDataset(
                firstNames,
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getFirstNames().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getFirstNames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapFirstNamesWithNonLatinScriptValue() {
        final String nonLatinScript = "nonLatinScript✨";
        final List<TransliterableMdsValue> firstNames = singletonList(createTransliterableValue(foo, nonLatinScript));

        final MatchingDataset matchingDataset = new MatchingDataset(
                firstNames,
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);
        final NonMatchingTransliterableAttribute nonMatchingTransliterableAttribute = nonMatchingAttributes.getFirstNames().get(0);

        assertThat(nonMatchingTransliterableAttribute.getValue()).isEqualTo(foo);
        assertThat(nonMatchingTransliterableAttribute.getNonLatinScriptValue()).isEqualTo(nonLatinScript);
    }

    @Test
    void shouldMapMiddleNamesOrderedByFromDate() {
        final List<SimpleMdsValue<String>> middleNames = asList(
                createSimpleMdsValue(fromTwo, foo),
                createSimpleMdsValue(fromThree, bar),
                createSimpleMdsValue(fromOne, baz),
                createSimpleMdsValue(fromFour, fuu));

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                middleNames,
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getMiddleNames().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getMiddleNames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapSurnamesOrderedByFromDate() {
        final List<TransliterableMdsValue> surnames = asList(
                createTransliterableValue(fromTwo, foo),
                createTransliterableValue(fromThree, bar),
                createTransliterableValue(fromOne, baz),
                createTransliterableValue(fromFour, fuu));

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                surnames,
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getSurnames().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getSurnames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapSurnamesWithNonLatinScriptValue() {
        final String nonLatinScript = "nonLatinScript";
        final List<TransliterableMdsValue> surnames = singletonList(createTransliterableValue(foo, nonLatinScript));

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                surnames,
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);
        final NonMatchingTransliterableAttribute nonMatchingTransliterableAttribute = nonMatchingAttributes.getSurnames().get(0);

        assertThat(nonMatchingTransliterableAttribute.getValue()).isEqualTo(foo);
        assertThat(nonMatchingTransliterableAttribute.getNonLatinScriptValue()).isEqualTo(nonLatinScript);
    }

    @Test
    void shouldMapDatesOfBirthOrderedByFromDate() {
        final LocalDate fooDate = LocalDate.now();
        final LocalDate barDate = LocalDate.now().minusDays(5);
        final LocalDate bazDate = LocalDate.now().minusDays(10);
        final LocalDate fuuDate = LocalDate.now().minusDays(15);
        final List<SimpleMdsValue<LocalDate>> datesOfBirth = asList(
                createDateValue(fromTwo, fooDate),
                createDateValue(fromThree, barDate),
                createDateValue(fromOne, bazDate),
                createDateValue(fromFour, fuuDate));

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                datesOfBirth,
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);
        final List<String> expectedDates = Stream.of(bazDate, fooDate, barDate, fuuDate)
                .map(LocalDate::toString)
                .collect(Collectors.toList());

        assertThat(nonMatchingAttributes.getDatesOfBirth().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(LocalDate::toString)
                .collect(Collectors.toList()))
                .isEqualTo(expectedDates);
        assertThat(nonMatchingAttributes.getDatesOfBirth()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapAddressesAndNotDiscardAttributes() {
        final Address addressIn = createAddress(fromOne, baz);

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                singletonList(addressIn),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);
        final NonMatchingAddress addressOut = nonMatchingAttributes.getAddresses().get(0).getValue();

        assertThat(addressOut.getPostCode()).isEqualTo(addressIn.getPostCode().get());
        assertThat(addressOut.getInternationalPostCode()).isEqualTo(addressIn.getInternationalPostCode().get());
        assertThat(addressOut.getUprn()).isEqualTo(addressIn.getUPRN().get());
        assertThat(addressOut.getLines()).isEqualTo(addressIn.getLines());

        assertThat(nonMatchingAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapCurrentAddressOrderedByFromDate() {
        final List<Address> currentAddress = asList(
                createAddress(fromTwo, foo),
                createAddress(fromThree, bar),
                createAddress(fromOne, baz),
                createAddress(fromFour, fuu));

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                currentAddress,
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getAddresses().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(NonMatchingAddress::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapPreviousAddressOrderedByFromDate() {
        final List<Address> previousAddress = asList(
                createAddress(fromTwo, foo),
                createAddress(fromThree, bar),
                createAddress(fromOne, baz),
                createAddress(fromFour, fuu));

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                previousAddress,
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getAddresses().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(NonMatchingAddress::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapAndMergeAddressOrderedByFromDate() {
        final List<Address> previousAddress = asList(
                createAddress(fromTwo, foo),
                createAddress(fromFour, fuu)
        );

        final List<Address> currentAddress = asList(
                createAddress(fromOne, baz),
                createAddress(fromThree, bar)
        );

        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                currentAddress,
                previousAddress,
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getAddresses().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(NonMatchingAddress::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    void shouldMapGender() {
        final Gender gender = Gender.NOT_SPECIFIED;
        final MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.of(new SimpleMdsValue<>(gender, null, null, true)),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);

        final NonMatchingAttributes nonMatchingAttributes = matchingDatasetMapper.mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getGender().getValue()).isEqualTo(gender);
    }

    @Test
    void sortTheListByToDateThenIsVerifiedThenFromDate() {
        final LocalDate now = LocalDate.now();
        final LocalDate fiveDaysAgo = now.minusDays(5);
        final LocalDate threeDaysAgo = now.minusDays(3);
        final NonMatchingVerifiableAttribute<String> attributeOne = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(null).withFrom(now).build();
        final NonMatchingVerifiableAttribute<String> attributeTwo = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(null).withFrom(fiveDaysAgo).build();
        final NonMatchingVerifiableAttribute<String> attributeThree = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(null).withFrom(now).build();
        final NonMatchingVerifiableAttribute<String> attributeFour = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(now).withFrom(now).build();
        final NonMatchingVerifiableAttribute<String> attributeFive = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(now).withFrom(fiveDaysAgo).build();
        final NonMatchingVerifiableAttribute<String> attributeSix = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(fiveDaysAgo).withFrom(now).build();
        final NonMatchingVerifiableAttribute<String> attributeSeven = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(fiveDaysAgo).withFrom(threeDaysAgo).build();
        final NonMatchingVerifiableAttribute<String> attributeEight = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(fiveDaysAgo).withFrom(null).build();
        final List<NonMatchingVerifiableAttribute<String>> unsorted = asList(
                attributeFour,
                attributeOne,
                attributeSix,
                attributeTwo,
                attributeSeven,
                attributeFive,
                attributeThree,
                attributeEight);

        assertThat(unsorted.stream().sorted(MatchingDatasetToNonMatchingAttributesMapper.attributeComparator()).collect(Collectors.toList())).isEqualTo(
                asList(
                        attributeOne,
                        attributeTwo,
                        attributeThree,
                        attributeFour,
                        attributeFive,
                        attributeSix,
                        attributeSeven,
                        attributeEight)
        );
    }

    private Comparator<NonMatchingVerifiableAttribute<?>> comparedByFromDate() {
        return Comparator.comparing(NonMatchingVerifiableAttribute::getFrom, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private Address createAddress(LocalDate from, String postCode) {
        return new Address(Collections.emptyList(), postCode, "BAR", "BAZ", from, null, true);
    }

    private TransliterableMdsValue createTransliterableValue(LocalDate from, String value) {
        return new TransliterableMdsValue(createSimpleMdsValue(from, value));
    }

    private TransliterableMdsValue createTransliterableValue(String value, String nonLatinScript) {
        return new TransliterableMdsValue(value, nonLatinScript);
    }

    private SimpleMdsValue<String> createSimpleMdsValue(LocalDate from, String value) {
        return new SimpleMdsValue<>(value, from, null, true);
    }

    private SimpleMdsValue<LocalDate> createDateValue(LocalDate from, LocalDate value) {
        return new SimpleMdsValue<>(value, from, null, true);
    }
}
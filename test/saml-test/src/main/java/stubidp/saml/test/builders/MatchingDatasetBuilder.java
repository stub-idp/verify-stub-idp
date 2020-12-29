package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetBuilder {
    private final List<TransliterableMdsValue> firstnames = new ArrayList<>();
    private final List<SimpleMdsValue<String>> middleNames = new ArrayList<>();
    private final List<TransliterableMdsValue> surnames = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private final List<SimpleMdsValue<Instant>> dateOfBirths = new ArrayList<>();
    private List<Address> currentAddresses = new ArrayList<>();
    private List<Address> previousAddresses = new ArrayList<>();
    private String personalId = "default-pid";

    private MatchingDatasetBuilder() {}
    
    public static MatchingDatasetBuilder aMatchingDataset() {
        return new MatchingDatasetBuilder();
    }

    public static MatchingDatasetBuilder aFullyPopulatedMatchingDataset() {
        final List<Address> currentAddressList = Collections.singletonList(new Address(Collections.singletonList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1999-03-15"), BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-02-09"), true));
        final List<Address> previousAddressList = Collections.singletonList(new Address(Collections.singletonList("previous-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1999-03-15"), BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-02-09"), true));
        final SimpleMdsValue<String> currentSurname = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-currentSurname").withVerifiedStatus(true).build();
        return aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-firstname").withVerifiedStatus(true).build())
                .addMiddleNames(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-middlename").withVerifiedStatus(true).build())
                .withSurnameHistory(Collections.singletonList(currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<Instant>aSimpleMdsValue().withValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(currentAddressList)
                .withPreviousAddresses(previousAddressList);
    }

    public MatchingDataset build() {
        return new MatchingDataset(firstnames, middleNames, surnames, gender, dateOfBirths, currentAddresses, previousAddresses, personalId);
    }

    public MatchingDatasetBuilder addFirstname(SimpleMdsValue<String> firstname) {
        this.firstnames.add(new TransliterableMdsValue(firstname));
        return this;
    }

    public MatchingDatasetBuilder addMiddleNames(SimpleMdsValue<String> middleNames) {
        this.middleNames.add(middleNames);
        return this;
    }

    public MatchingDatasetBuilder addSurname(SimpleMdsValue<String> surname) {
        this.surnames.add(new TransliterableMdsValue(surname));
        return this;
    }

    public MatchingDatasetBuilder withGender(SimpleMdsValue<Gender> gender) {
        this.gender = Optional.ofNullable(gender);
        return this;
    }

    public MatchingDatasetBuilder withPersonalId(String personalId) {
        this.personalId = personalId;
        return this;
    }

    public MatchingDatasetBuilder addDateOfBirth(SimpleMdsValue<Instant> dateOfBirth) {
        this.dateOfBirths.add(dateOfBirth);
        return this;
    }

    public MatchingDatasetBuilder withCurrentAddresses(List<Address> currentAddresses) {
        this.currentAddresses = currentAddresses;
        return this;
    }

    public MatchingDatasetBuilder withPreviousAddresses(List<Address> previousAddresses) {
        this.previousAddresses = previousAddresses;
        return this;
    }

    public MatchingDatasetBuilder withoutFirstName() {
        this.firstnames.clear();
        return this;
    }

    public MatchingDatasetBuilder withoutMiddleName() {
        this.middleNames.clear();
        return this;
    }

    public MatchingDatasetBuilder withoutSurname() {
        this.surnames.clear();
        return this;
    }

    public MatchingDatasetBuilder withoutDateOfBirth() {
        this.dateOfBirths.clear();
        return this;
    }

    public MatchingDatasetBuilder withSurnameHistory(
            final List<SimpleMdsValue<String>> surnameHistory) {

        this.surnames.clear();
        this.surnames.addAll(surnameHistory.stream().map(TransliterableMdsValue::new).collect(Collectors.toList()));
        return this;
    }
}

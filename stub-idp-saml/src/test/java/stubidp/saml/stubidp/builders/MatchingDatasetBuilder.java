package stubidp.saml.stubidp.builders;

import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.core.domain.Address;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.saml.utils.core.domain.MatchingDataset;
import stubidp.saml.utils.core.domain.SimpleMdsValue;
import stubidp.saml.utils.core.domain.TransliterableMdsValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class MatchingDatasetBuilder {
    private List<TransliterableMdsValue> firstNames = new ArrayList<>();
    private List<SimpleMdsValue<String>> middleNames = new ArrayList<>();
    private List<TransliterableMdsValue> surnames = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private List<SimpleMdsValue<Instant>> dateOfBirths = new ArrayList<>();
    private List<Address> currentAddresses = new ArrayList<>();
    private List<Address> previousAddresses = new ArrayList<>();
    private String personalId = "";

    public static MatchingDatasetBuilder aMatchingDataset() {
        return new MatchingDatasetBuilder();
    }

    public static MatchingDatasetBuilder aFullyPopulatedMatchingDataset() {
        final List<Address> currentAddressList = singletonList(new AddressFactory().create(singletonList("subject-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", "1999-03-15", "2000-02-09", true));
        final List<Address> previousAddressList = singletonList(new AddressFactory().create(singletonList("previous-address-line-1"), "subject-address-post-code", "internation-postcode", "uprn", "1999-03-15", "2000-02-09", true));
        final TransliterableMdsValue currentSurname = TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-currentSurname").withVerifiedStatus(true).build();
        return aMatchingDataset()
                .addFirstName(TransliterableMdsValueBuilder.asTransliterableMdsValue().withValue("subject-firstname").withVerifiedStatus(true).build())
                .addMiddleNames(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("subject-middlename").withVerifiedStatus(true).build())
                .withSurnameHistory(singletonList(currentSurname))
                .withGender(SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<Instant>aSimpleMdsValue().withValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-02-09")).withVerifiedStatus(true).build())
                .withCurrentAddresses(currentAddressList)
                .withPreviousAddresses(previousAddressList)
                .withPersonalId("personalId");
    }

    public MatchingDataset build() {
        return new MatchingDataset(firstNames, middleNames, surnames, gender, dateOfBirths, currentAddresses, previousAddresses, personalId);
    }

    public MatchingDatasetBuilder addFirstName(TransliterableMdsValue firstName) {
        this.firstNames.add(firstName);
        return this;
    }

    public MatchingDatasetBuilder addMiddleNames(SimpleMdsValue<String> middleNames) {
        this.middleNames.add(middleNames);
        return this;
    }

    public MatchingDatasetBuilder addSurname(TransliterableMdsValue surname) {
        this.surnames.add(surname);
        return this;
    }

    public MatchingDatasetBuilder withGender(SimpleMdsValue<Gender> gender) {
        this.gender = Optional.ofNullable(gender);
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
        this.firstNames.clear();
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
            final List<TransliterableMdsValue> surnameHistory) {

        this.surnames.clear();
        this.surnames.addAll(surnameHistory);
        return this;
    }

    public MatchingDatasetBuilder withPersonalId(String personalId) {
        this.personalId = personalId;
        return this;
    }
}

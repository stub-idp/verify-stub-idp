package stubidp.saml.utils.core.transformers;

import stubidp.saml.utils.core.domain.Address;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.saml.utils.core.domain.MatchingDataset;
import stubidp.saml.utils.core.domain.SimpleMdsValue;
import stubidp.saml.utils.core.domain.TransliterableMdsValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchingDatasetBuilder {
    private List<TransliterableMdsValue> firstnames = new ArrayList<>();
    private List<SimpleMdsValue<String>> middlenames = new ArrayList<>();
    private List<TransliterableMdsValue> surnames = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private List<SimpleMdsValue<Instant>> dateOfBirths = new ArrayList<>();
    private List<Address> currentAddresses = new ArrayList<>();
    private List<Address> previousAddresses = new ArrayList<>();
    private String personalId;

    public void addFirstNames(List<TransliterableMdsValue> firstnames) {
        this.firstnames.addAll(firstnames);
    }

    public void addSurnames(List<TransliterableMdsValue> surnames) {
        this.surnames.addAll(surnames);
    }

    public void gender(SimpleMdsValue<Gender> gender) {
        this.gender = Optional.ofNullable(gender);
    }

    public void dateOfBirth(List<SimpleMdsValue<Instant>> dateOfBirths) {
        this.dateOfBirths.addAll(dateOfBirths);
    }

    public void addCurrentAddresses(List<Address> currentAddresses) {
        this.currentAddresses.addAll(currentAddresses);
    }

    public void personalId(String personalId) {
        this.personalId = personalId;
    }

    public void middlenames(List<SimpleMdsValue<String>> middlenames) {
        this.middlenames.addAll(middlenames);
    }

    public void addPreviousAddresses(List<Address> previousAddresses) {
        this.previousAddresses.addAll(previousAddresses);
    }

    public MatchingDataset build() {
        return new MatchingDataset(
                firstnames,
                middlenames,
                surnames,
                gender,
                dateOfBirths,
                currentAddresses,
                previousAddresses,
                personalId
        );
    }
}

package stubidp.saml.domain.assertions;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatchingDataset {
    private final List<TransliterableMdsValue> firstNames;
    private final List<SimpleMdsValue<String>> middleNames;
    private final List<TransliterableMdsValue> surnames;
    private final Optional<SimpleMdsValue<Gender>> gender;
    private final List<SimpleMdsValue<Instant>> dateOfBirths;
    private final List<Address> currentAddresses;
    private final List<Address> previousAddresses;
    private final String personalId;

    public MatchingDataset(
            List<TransliterableMdsValue> firstNames,
            List<SimpleMdsValue<String>> middleNames,
            List<TransliterableMdsValue> surnames,
            Optional<SimpleMdsValue<Gender>> gender,
            List<SimpleMdsValue<Instant>> dateOfBirths,
            List<Address> currentAddresses,
            List<Address> previousAddresses,
            String personalId) {
        this.firstNames = firstNames;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirths = dateOfBirths;
        this.currentAddresses = currentAddresses;
        this.previousAddresses = previousAddresses;
        this.personalId = personalId;
    }

    public List<TransliterableMdsValue> getFirstNames() {
        return firstNames;
    }

    public List<SimpleMdsValue<String>> getMiddleNames() {
        return middleNames;
    }

    public List<TransliterableMdsValue> getSurnames() {
        return surnames;
    }

    public Optional<SimpleMdsValue<Gender>> getGender() {
        return gender;
    }

    public List<SimpleMdsValue<Instant>> getDateOfBirths() {
        return dateOfBirths;
    }

    public List<Address> getCurrentAddresses() {
        return currentAddresses;
    }

    public List<Address> getPreviousAddresses() {
        return previousAddresses;
    }

    public List<Address> getAddresses() {
        return Stream.<Address>concat(currentAddresses.stream(), previousAddresses.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    public String getPersonalId() {
        return personalId;
    }
}

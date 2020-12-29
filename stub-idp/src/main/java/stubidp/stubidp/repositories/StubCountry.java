package stubidp.stubidp.repositories;

import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseEidasUser;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class StubCountry {

    private final String friendlyId;
    private final String displayName;
    private final String assetId;
    private final String issuerId;
    private final AllIdpsUserRepository allIdpsUserRepository;

    public StubCountry(String friendlyId, String displayName, String assetId, String issuerId, AllIdpsUserRepository allIdpsUserRepository) {
        this.friendlyId = friendlyId;
        this.displayName = displayName;
        this.assetId = assetId;
        this.issuerId = issuerId;
        this.allIdpsUserRepository = allIdpsUserRepository;
    }

    public Optional<DatabaseEidasUser> getUser(String username, String password) {
        Optional<DatabaseEidasUser> userForStubCountry = allIdpsUserRepository.getUserForCountry(friendlyId, username);
        if (userForStubCountry.isPresent() && BCrypt.checkpw(password, userForStubCountry.get().getPassword())) {
            return userForStubCountry;
        }

        return Optional.empty();
    }

    public DatabaseEidasUser createUser(String username, String password,
                                        SimpleMdsValue<String> firstName,
                                        Optional<SimpleMdsValue<String>> nonLatinFirstName,
                                        SimpleMdsValue<String> surname,
                                        Optional<SimpleMdsValue<String>> nonLatinSurname,
                                        SimpleMdsValue<Instant> dateOfBirth,
                                        AuthnContext levelOfAssurance){
        return allIdpsUserRepository.createUserForStubCountry(
                friendlyId, UUID.randomUUID().toString(),
                username, password,
                firstName, nonLatinFirstName,
                surname, nonLatinSurname,
                dateOfBirth,
                levelOfAssurance
        );
    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public boolean userExists(String username) {
        return allIdpsUserRepository.containsUserForIdp(friendlyId, username);
    }
}

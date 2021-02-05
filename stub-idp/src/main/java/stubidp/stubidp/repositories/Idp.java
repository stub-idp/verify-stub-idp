package stubidp.stubidp.repositories;

import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseIdpUser;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Idp {

    private final String friendlyId;
    private final String displayName;
    private final String assetId;
    private final boolean sendKeyInfo;
    private final String issuerId;
    private final AllIdpsUserRepository allIdpsUserRepository;

    public Idp(String displayName, String friendlyId, String assetId, boolean sendKeyInfo, String issuerId, AllIdpsUserRepository allIdpsUserRepository) {
        this.friendlyId = friendlyId;
        this.displayName = displayName;
        this.assetId = assetId;
        this.sendKeyInfo = sendKeyInfo;
        this.issuerId = issuerId;
        this.allIdpsUserRepository = allIdpsUserRepository;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public String getAssetId() {
        return assetId;
    }

    public boolean shouldSendKeyInfo() {
        return sendKeyInfo;
    }

    public Optional<DatabaseIdpUser> getUser(String username, String password) {
        Optional<DatabaseIdpUser> userForIdp = allIdpsUserRepository.getUserForIdp(friendlyId, username);
        if (userForIdp.isPresent() && BCrypt.checkpw(password, userForIdp.get().getPassword())) {
            return userForIdp;
        }

        return Optional.empty();
    }

    public DatabaseIdpUser createUser(
            Optional<String> pid,
            List<SimpleMdsValue<String>> firstnames,
            List<SimpleMdsValue<String>> middleNames,
            List<SimpleMdsValue<String>> surnames,
            Optional<SimpleMdsValue<Gender>> gender,
            List<SimpleMdsValue<LocalDate>> dateOfBirths,
            List<Address> addresses,
            String username,
            String password,
            AuthnContext levelOfAssurance) {

        String pidValue = pid.orElseGet(() -> UUID.randomUUID().toString());
        return allIdpsUserRepository.createUserForIdp(friendlyId, pidValue, firstnames, middleNames, surnames, gender, dateOfBirths, addresses, username, password, levelOfAssurance);
    }

    public void deleteUser(String username) {
        allIdpsUserRepository.deleteUserFromIdp(friendlyId, username);
    }

    public boolean userExists(String username) {
        return allIdpsUserRepository.containsUserForIdp(friendlyId, username);
    }

    public Optional<DatabaseIdpUser> getUser(String username) {
        return allIdpsUserRepository.getUserForIdp(friendlyId, username);
    }

    public Collection<DatabaseIdpUser> getAllUsers() {
        return allIdpsUserRepository.getAllUsersForIdp(friendlyId);
    }

    public String getIssuerId() {
        return issuerId;
    }
}

package stubidp.stubidp.views.helpers;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseIdpUser;

import java.time.LocalDate;

import static java.util.stream.Collectors.joining;

public class IdpUserHelper {

    private final DatabaseIdpUser idpUser;

    public IdpUserHelper(DatabaseIdpUser databaseIdpUser) { this.idpUser = databaseIdpUser; }

    public DatabaseIdpUser getIdpUser() { return idpUser; }

    public String getFirstName() {
        return idpUser.getFirstnames()
                .stream()
                .findFirst()
                .orElseGet(this::createEmptySimpleMdsStringValue)
                .getValue();
    }

    private SimpleMdsValue<String> createEmptySimpleMdsStringValue() {
        return new SimpleMdsValue<>("", null, null, true);
    }

    public String getSurname() {
        return idpUser.getSurnames()
                .stream()
                .findFirst()
                .orElseGet(this::createEmptySimpleMdsStringValue)
                .getValue();
    }

    public String getSurnames() {
        return this.idpUser
                .getSurnames()
                .stream()
                .map(SimpleMdsValue::getValue)
                .collect(joining(","));
    }

    public String getDateOfBirth() {
        return idpUser.getDateOfBirths()
                .stream()
                .findFirst()
                .map(SimpleMdsValue::getValue)
                .map(LocalDate::toString)
                .orElse("");
    }

    public String getGender() {
        return idpUser.getGender()
                .map(gender -> gender.getValue().getValue())
                .orElse("");
    }

    public Address getAddress() {
        return this.idpUser.getCurrentAddress();
    }

    public String getLoa() { return this.idpUser.getLevelOfAssurance().toString(); }

}

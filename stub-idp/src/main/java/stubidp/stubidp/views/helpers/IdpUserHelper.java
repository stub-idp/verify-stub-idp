package stubidp.stubidp.views.helpers;

import org.apache.commons.lang.StringUtils;
import stubidp.saml.utils.core.domain.Address;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;

import java.util.Collection;
import java.util.stream.Collectors;

public class IdpUserHelper {

    DatabaseIdpUser idpUser;

    public IdpUserHelper(DatabaseIdpUser databaseIdpUser) { this.idpUser = databaseIdpUser; }

    public DatabaseIdpUser getIdpUser() { return idpUser; }

    public String getFirstName() {
        if (!idpUser.getFirstnames().isEmpty()) {
            return idpUser.getFirstnames().get(0).getValue();
        }
        return createEmptySimpleMdsStringValue().getValue();
    }

    private MatchingDatasetValue<String> createEmptySimpleMdsStringValue() {
        return new MatchingDatasetValue<>("", null, null, true);
    }

    public String getSurname() {
        if (!idpUser.getSurnames().isEmpty()) {
            return idpUser.getSurnames().get(0).getValue();
        }
        return createEmptySimpleMdsStringValue().getValue();
    }

    public String getSurnames() {
        Collection<String> surnameValues = this.idpUser.getSurnames().stream()
                .map(s -> s != null ? s.getValue() : null)
                .collect(Collectors.toList());
        return StringUtils.join(surnameValues, ",");
    }

    public String getDateOfBirth() {
        if (!idpUser.getDateOfBirths().isEmpty()) {
            return idpUser.getDateOfBirths().get(0).getValue().toString("dd/MM/yyyy");
        }
        return "";
    }

    public String getGender() {
        if (idpUser.getGender().isPresent()) {
            return idpUser.getGender().get().getValue().getValue();
        }
        return "";
    }

    public Address getAddress() {
        return this.idpUser.getCurrentAddress();
    }

    public String getLoa() { return this.idpUser.getLevelOfAssurance().toString(); }

}

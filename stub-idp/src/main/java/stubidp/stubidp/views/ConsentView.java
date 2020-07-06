package stubidp.stubidp.views;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.views.helpers.IdpUserHelper;

import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConsentView extends IdpPageView {
    private final boolean userLOADidNotMatch;
    private final AuthnContext userLevelOfAssurance;
    private final List<AuthnContext> levelsOfAssurance;
    private final IdpUserHelper idpUserHelper;

    public ConsentView(String name, String idpId, String assetId, DatabaseIdpUser idpUser, boolean userLOADidNotMatch, AuthnContext userLevelOfAssurance, List<AuthnContext> levelsOfAssurance, String csrfToken) {
        super("consent.ftl", name, idpId, null, assetId, Optional.ofNullable(csrfToken));
        this.userLOADidNotMatch = userLOADidNotMatch;
        this.userLevelOfAssurance = userLevelOfAssurance;
        this.levelsOfAssurance = levelsOfAssurance;
        this.idpUserHelper = new IdpUserHelper(idpUser);
    }

    public String getPageTitle() {
        return String.format("Consent page for %s", getName());
    }

    public String getLoaMismatchMessage() {
        String requestedLOAs = levelsOfAssurance.stream().map(Enum::name).collect(Collectors.joining(", "));
        return String.format("User's LOA [%s] does not match with requested LOAs [%s]", userLevelOfAssurance.name(), requestedLOAs);
    }

    public DatabaseIdpUser getIdpUser() {
        return idpUserHelper.getIdpUser();
    }

    public String getFirstName() {
        return idpUserHelper.getFirstName();
    }

    public String getSurname() {
        return idpUserHelper.getSurname();
    }

    public String getDateOfBirth() {
        return idpUserHelper.getDateOfBirth();
    }

    public String getGender() {
        return idpUserHelper.getGender();
    }

    public Address getAddress() {
        return idpUserHelper.getAddress();
    }

    public String getLoa() {
        return idpUserHelper.getLoa();
    }

    public boolean isUserLOADidNotMatch() {
        return userLOADidNotMatch;
    }

    public String getConsentResource() {
        return UriBuilder.fromPath(Urls.IDP_CONSENT_RESOURCE).build(idpId).toASCIIString();
    }
}

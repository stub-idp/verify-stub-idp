package stubidp.stubidp.views;

import stubidp.stubidp.Urls;

import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

public class EidasLoginPageView extends IdpPageView {

    public EidasLoginPageView(String name, String schemeId, String errorMessage, String assetId, String csrfToken) {
        super("eidasLoginPage.ftl", name, schemeId, errorMessage, assetId, Optional.ofNullable(csrfToken));
    }

    public String getPageTitle() {
        return String.format("Welcome to %s", getName());
    }

    public String getEidasAuthnFailureResource() {
        return UriBuilder.fromPath(Urls.EIDAS_AUTHN_FAILURE_RESOURCE).build(idpId).toASCIIString();
    }

    public String getSignAssertionsCheckboxGroup() { return Urls.SIGN_ASSERTIONS_PARAM; }

    public String getSignAssertionsCheckboxValue() { return SignAssertions.signAssertions.name(); }
}

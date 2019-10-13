package stubidp.stubidp.views;

import stubidp.stubidp.Urls;

import java.util.Optional;

public class EidasLoginPageView extends IdpPageView {

    public EidasLoginPageView(String name, String schemeId, String errorMessage, String assetId, String csrfToken) {
        super("eidasLoginPage.ftl", name, schemeId, errorMessage, assetId, Optional.ofNullable(csrfToken));
    }

    public String getPageTitle() {
        return String.format("Welcome to %s", getName());
    }

    public String getEidasAuthnFailureResource() {
        return Urls.EIDAS_AUTHN_FAILURE_RESOURCE;
    }

    public String getSignAssertionsCheckboxGroup() { return Urls.SIGN_ASSERTIONS_PARAM; }

    public String getSignAssertionsCheckboxValue() { return SignAssertions.signAssertions.name(); }
}

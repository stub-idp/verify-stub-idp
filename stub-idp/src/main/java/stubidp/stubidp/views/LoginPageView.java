package stubidp.stubidp.views;

import stubidp.stubidp.Urls;

import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

public class LoginPageView extends IdpPageView {

    public LoginPageView(String name, String idpId, String errorMessage, String assetId, String csrfToken) {
        super("loginPage.ftl", name, idpId, errorMessage, assetId, Optional.ofNullable(csrfToken));
    }

    public String getPageTitle() {
        return String.format("Welcome to %s", getName());
    }

    public String getAuthnPendingResource() {
        return UriBuilder.fromPath(Urls.IDP_AUTHN_PENDING_RESOURCE).build(idpId).toASCIIString();
    }

    public String getNoAuthnContextResource() {
        return UriBuilder.fromPath(Urls.IDP_NO_AUTHN_CONTEXT_RESOURCE).build(idpId).toASCIIString();
    }

    public String getAuthnFailureResource() {
        return UriBuilder.fromPath(Urls.IDP_AUTHN_FAILURE_RESOURCE).build(idpId).toASCIIString();
    }

    public String getUpliftFailedResource() {
        return UriBuilder.fromPath(Urls.IDP_UPLIFT_FAILED_RESOURCE).build(idpId).toASCIIString();
    }

    public String getFraudFailureResource() {
        return UriBuilder.fromPath(Urls.IDP_FRAUD_FAILURE_RESOURCE).build(idpId).toASCIIString();
    }

    public String getRequesterErrorResource() {
        return UriBuilder.fromPath(Urls.IDP_REQUESTER_ERROR_RESOURCE).build(idpId).toASCIIString();
    }

}

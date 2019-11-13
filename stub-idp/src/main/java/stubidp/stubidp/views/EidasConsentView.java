package stubidp.stubidp.views;

import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.EidasUser;

import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

public class EidasConsentView extends IdpPageView {
    private final EidasUser user;

    public EidasConsentView(String name, String idpId, String assetId, EidasUser user, String csrfToken) {
        super("eidasConsent.ftl", name, idpId, null, assetId, Optional.ofNullable(csrfToken));
        this.user = user;
    }

    public String getPageTitle() {
        return String.format("Consent page for %s", getName());
    }

    public EidasUser getUser() {
        return user;
    }

    public String getEidasConsentResource() {
        return UriBuilder.fromPath(Urls.EIDAS_CONSENT_RESOURCE).build(idpId).toASCIIString();
    }

}

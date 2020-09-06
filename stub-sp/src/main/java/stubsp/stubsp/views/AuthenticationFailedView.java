package stubsp.stubsp.views;

import stubsp.stubsp.Urls;
import stubsp.stubsp.domain.SamlResponseFromIdpDto;

public class AuthenticationFailedView extends StubSpView {
    private final SamlResponseFromIdpDto samlResponseFromIdpDto;

    public AuthenticationFailedView(SamlResponseFromIdpDto samlResponseFromIdpDto) {
        super("Authentication Failed", "authenticationFailed.ftl");
        this.samlResponseFromIdpDto = samlResponseFromIdpDto;
    }

    public String getRootResource() {
        return Urls.ROOT_RESOURCE;
    }

    public String getSecureResource() {
        return Urls.SECURE_RESOURCE;
    }

    public SamlResponseFromIdpDto getSamlResponseFromIdpDto() {
        return samlResponseFromIdpDto;
    }
}

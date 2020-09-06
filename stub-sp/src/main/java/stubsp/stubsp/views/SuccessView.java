package stubsp.stubsp.views;

import stubsp.stubsp.Urls;
import stubsp.stubsp.domain.SamlResponseFromIdpDto;

public class SuccessView extends StubSpView {
    private final SamlResponseFromIdpDto samlResponseFromIdpDto;

    public SuccessView(SamlResponseFromIdpDto samlResponseFromIdpDto) {
        super("Authentication Success", "success.ftl");
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

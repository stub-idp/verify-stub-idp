package stubsp.stubsp.views;

import stubsp.stubsp.Urls;

public class RootView extends StubSpView {
    public RootView(String pageTitle) {
        super(pageTitle,"rootView.ftl");
    }

    public String getMetadataResource() {
        return Urls.SAML_FEDERATION_METADATA_RESOURCE;
    }

    public String getSecureResource() {
        return Urls.SECURE_RESOURCE;
    }
}

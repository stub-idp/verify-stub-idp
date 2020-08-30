package stubsp.stubsp.views;

import stubsp.stubsp.Urls;

public class SuccessView extends StubSpView {
    public SuccessView() {
        super("Authentication Success", "success.ftl");
    }

    public String getRootResource() {
        return Urls.ROOT_RESOURCE;
    }

    public String getSecureResource() {
        return Urls.SECURE_RESOURCE;
    }
}

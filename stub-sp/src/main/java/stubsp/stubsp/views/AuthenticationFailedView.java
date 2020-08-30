package stubsp.stubsp.views;

import stubsp.stubsp.Urls;

public class AuthenticationFailedView extends StubSpView {
    public AuthenticationFailedView() {
        super("Authentication Failed", "authenticationFailed.ftl");
    }

    public String getRootResource() {
        return Urls.ROOT_RESOURCE;
    }

    public String getSecureResource() {
        return Urls.SECURE_RESOURCE;
    }
}

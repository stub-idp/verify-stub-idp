package stubsp.stubsp.views;

import stubsp.stubsp.Urls;

public class SecureView extends StubSpView {
    public SecureView() {
        super("Secure!!!!1!!1!", "secureView.ftl");
    }

    public String getRootResource() {
        return Urls.ROOT_RESOURCE;
    }
}

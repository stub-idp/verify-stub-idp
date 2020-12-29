package stubidp.test.integration.support;

import stubidp.stubidp.configuration.StubIdp;
import stubidp.stubidp.configuration.UserCredentials;

import java.util.ArrayList;
import java.util.List;

public class StubIdpBuilder {
    private String assetId = "default-stub-idp";
    private String displayName = "Default Stub IDP";
    private String friendlyId = "default-stub-idp";
    private final List<UserCredentials> idpUserCredentials = new ArrayList<>();
    private boolean sendKeyInfo = false;

    public static StubIdpBuilder aStubIdp() {
        return new StubIdpBuilder();
    }

    public StubIdp build() {
        if (idpUserCredentials.isEmpty()) {
            idpUserCredentials.add(new TestUserCredentials("foo", "bar"));
        }

        return new TestStubIdp(assetId, displayName, friendlyId, idpUserCredentials, sendKeyInfo);
    }

    public StubIdpBuilder withId(String id) {
        this.assetId = id;
        this.friendlyId = id;
        return this;
    }

    public StubIdpBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public StubIdpBuilder addUserCredentials(UserCredentials credentials) {
        this.idpUserCredentials.add(credentials);
        return this;
    }

    public StubIdpBuilder sendKeyInfo(boolean sendKeyInfo) {
        this.sendKeyInfo = sendKeyInfo;
        return this;
    }
}

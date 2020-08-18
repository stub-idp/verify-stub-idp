package stubidp.test.integration.support;

import stubidp.stubidp.configuration.StubIdp;
import stubidp.stubidp.configuration.UserCredentials;

import java.util.List;

public class TestStubIdp extends StubIdp {

    public TestStubIdp(String assetId, String displayName, String friendlyId, List<UserCredentials> idpUserCredentials, boolean sendKeyInfo) {
        this.sendKeyInfo = sendKeyInfo;
        this.assetId = assetId;
        this.displayName = displayName;
        this.friendlyId = friendlyId;
        this.idpUserCredentials = idpUserCredentials;
    }

}

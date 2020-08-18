package stubidp.kotlin.test.integration.support

import stubidp.stubidp.configuration.StubIdp
import stubidp.stubidp.configuration.UserCredentials

class TestStubIdp(assetId: String?, displayName: String?, friendlyId: String?, idpUserCredentials: List<UserCredentials?>?, sendKeyInfo: Boolean) : StubIdp() {
    init {
        this.sendKeyInfo = sendKeyInfo
        this.assetId = assetId
        this.displayName = displayName
        this.friendlyId = friendlyId
        this.idpUserCredentials = idpUserCredentials
    }
}
package stubidp.kotlin.test.integration.support

import stubidp.stubidp.configuration.StubIdp
import stubidp.stubidp.configuration.UserCredentials
import stubidp.test.integration.support.TestStubIdp
import stubidp.test.integration.support.TestUserCredentials
import java.util.ArrayList

class StubIdpBuilder {
    private var assetId = "default-stub-idp"
    private var displayName = "Default Stub IDP"
    private var friendlyId = "default-stub-idp"
    private val idpUserCredentials: MutableList<UserCredentials> = ArrayList()
    private var sendKeyInfo = false
    
    fun build(): StubIdp {
        if (idpUserCredentials.isEmpty()) {
            idpUserCredentials.add(TestUserCredentials("foo", "bar"))
        }
        return TestStubIdp(assetId, displayName, friendlyId, idpUserCredentials, sendKeyInfo)
    }

    fun withId(id: String): StubIdpBuilder {
        assetId = id
        friendlyId = id
        return this
    }

    fun withDisplayName(displayName: String): StubIdpBuilder {
        this.displayName = displayName
        return this
    }

    fun addUserCredentials(credentials: UserCredentials): StubIdpBuilder {
        idpUserCredentials.add(credentials)
        return this
    }

    fun sendKeyInfo(sendKeyInfo: Boolean): StubIdpBuilder {
        this.sendKeyInfo = sendKeyInfo
        return this
    }

    companion object {
        @JvmStatic
        fun aStubIdp(): StubIdpBuilder {
            return StubIdpBuilder()
        }
    }
}
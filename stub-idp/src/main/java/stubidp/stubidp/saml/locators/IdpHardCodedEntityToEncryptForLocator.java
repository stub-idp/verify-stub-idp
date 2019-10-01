package stubidp.stubidp.saml.locators;

import stubidp.saml.security.EntityToEncryptForLocator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static stubidp.stubidp.StubIdpIdpBinder.HUB_ENTITY_ID;

@Singleton
public class IdpHardCodedEntityToEncryptForLocator implements EntityToEncryptForLocator {

    private final String hubEntityId;

    @Inject
    public IdpHardCodedEntityToEncryptForLocator(@Named(HUB_ENTITY_ID) String hubEntityId) {
        this.hubEntityId = hubEntityId;
    }

    @Override
    public String fromRequestId(String requestId) {
        return hubEntityId;
    }
}

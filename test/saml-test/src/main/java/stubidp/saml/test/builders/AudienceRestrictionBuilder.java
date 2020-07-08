package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AudienceRestriction;
import stubidp.saml.test.OpenSamlXmlObjectFactory;
import stubidp.test.devpki.TestEntityIds;

public class AudienceRestrictionBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private String audienceId = TestEntityIds.HUB_ENTITY_ID;

    private AudienceRestrictionBuilder() {}

    public static AudienceRestrictionBuilder anAudienceRestriction() {
        return new AudienceRestrictionBuilder();
    }

    public AudienceRestriction build() {
        return openSamlXmlObjectFactory.createAudienceRestriction(audienceId);
    }

    public AudienceRestrictionBuilder withAudienceId(String audienceId) {
        this.audienceId = audienceId;
        return this;
    }
}

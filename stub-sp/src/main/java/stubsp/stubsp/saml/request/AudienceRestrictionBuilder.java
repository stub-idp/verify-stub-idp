package stubsp.stubsp.saml.request;

import org.opensaml.saml.saml2.core.AudienceRestriction;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

public class AudienceRestrictionBuilder {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private String audienceId;

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

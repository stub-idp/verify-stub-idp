package stubidp.saml.utils.core.test.builders;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConditionsBuilder {

    private XMLObjectBuilderFactory xmlObjectBuilderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    private List<AudienceRestriction> audienceRestrictions = new ArrayList<>();
    private AudienceRestriction defaultAudienceRestriction = AudienceRestrictionBuilder.anAudienceRestriction().build();
    private boolean shouldIncludeDefaultAudienceRestriction = true;
    private Instant notBefore = null;
    private Instant notOnOrAfter = null;

    public static ConditionsBuilder aConditions() {
        return new ConditionsBuilder();
    }

    public Conditions build() {
        Conditions conditions = (Conditions)xmlObjectBuilderFactory
                .getBuilder(Conditions.DEFAULT_ELEMENT_NAME)
                .buildObject(Conditions.DEFAULT_ELEMENT_NAME, Conditions.TYPE_NAME);

        if (shouldIncludeDefaultAudienceRestriction) {
            audienceRestrictions.add(defaultAudienceRestriction);
        }
        conditions.getAudienceRestrictions().addAll(audienceRestrictions);
        conditions.setNotBefore(notBefore);
        conditions.setNotOnOrAfter(notOnOrAfter);

        return conditions;
    }

    public ConditionsBuilder withoutDefaultAudienceRestriction() {
        shouldIncludeDefaultAudienceRestriction = false;
        return this;
    }

    public ConditionsBuilder addAudienceRestriction(AudienceRestriction audienceRestriction) {
        shouldIncludeDefaultAudienceRestriction = false;
        audienceRestrictions.add(audienceRestriction);
        return this;
    }

    public ConditionsBuilder withNotBefore(Instant notBefore){
        this.notBefore = notBefore;
        return this;
    }

    public ConditionsBuilder withNotOnOrAfter(Instant notOnOrAfter){
        this.notOnOrAfter = notOnOrAfter;
        return this;
    }

    public ConditionsBuilder validFor(Duration duration){
        return withNotBefore(Instant.now())
              .withNotOnOrAfter(Instant.now().plus(duration));
    }

    public ConditionsBuilder restrictedToAudience(String audienceUri){
        AudienceRestriction audienceRestriction = AudienceRestrictionBuilder.anAudienceRestriction().withAudienceId(audienceUri).build();
        return addAudienceRestriction(audienceRestriction);
    }
}

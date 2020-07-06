package stubidp.saml.utils.core.test.builders;

import stubidp.saml.domain.assertions.AssertionRestrictions;

import java.time.Instant;
import java.time.ZoneId;

public class AssertionRestrictionsBuilder {

    private Instant notOnOrAfter = Instant.now().atZone(ZoneId.of("UTC")).plusDays(2).toInstant();
    private String inResponseTo = "default-in-response-to";
    private String recipient = "recipient";

    public static AssertionRestrictionsBuilder anAssertionRestrictions() {
        return new AssertionRestrictionsBuilder();
    }

    public AssertionRestrictions build() {
        return new AssertionRestrictions(
                notOnOrAfter,
                inResponseTo,
                recipient);
    }

    public AssertionRestrictionsBuilder withNotOnOrAfter(Instant notOnOrAfter) {
        this.notOnOrAfter = notOnOrAfter;
        return this;
    }

    public AssertionRestrictionsBuilder withInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
        return this;
    }

    public AssertionRestrictionsBuilder withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }
}

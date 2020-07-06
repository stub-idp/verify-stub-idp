package stubidp.stubidp.domain.factories;

import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.stubidp.configuration.AssertionLifetimeConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.time.Instant;

import static stubidp.stubidp.StubIdpIdpBinder.SP_ENTITY_ID;

public class AssertionRestrictionsFactory {
    private final Duration assertionLifetime;
    private final String hubEntityId;

    @Inject
    public AssertionRestrictionsFactory(AssertionLifetimeConfiguration assertionTimeoutConfig,
                                        @Named(SP_ENTITY_ID) String hubEntityId) {
        this.assertionLifetime = Duration.ofMillis(assertionTimeoutConfig.getAssertionLifetime().toMilliseconds());
        this.hubEntityId = hubEntityId;
    }

    public AssertionRestrictions createRestrictionsForSendingToHub(String inResponseTo) {
        return new AssertionRestrictions(Instant.now().plus(assertionLifetime), inResponseTo, hubEntityId);
    }

    public AssertionRestrictions create(String inResponseTo, String recipient) {
        return new AssertionRestrictions(Instant.now().plus(assertionLifetime), inResponseTo, recipient);
    }
}

package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;

public class MatchingServiceAuthnStatementToAuthnStatementTransformer {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory;
    private final Clock clock;

    @Inject
    public MatchingServiceAuthnStatementToAuthnStatementTransformer(
            final OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        this(openSamlXmlObjectFactory, Clock.systemUTC());
    }

    MatchingServiceAuthnStatementToAuthnStatementTransformer(
            final OpenSamlXmlObjectFactory openSamlXmlObjectFactory,
            final Clock clock) {

        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
        this.clock = clock;
    }

    public AuthnStatement transform(MatchingServiceAuthnStatement idaAuthnStatement) {
        AuthnStatement authnStatement = openSamlXmlObjectFactory.createAuthnStatement();
        AuthnContext authnContext = openSamlXmlObjectFactory.createAuthnContext();
        authnContext.setAuthnContextClassRef(openSamlXmlObjectFactory.createAuthnContextClassReference(idaAuthnStatement.getAuthnContext().getUri()));
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(Instant.now(clock));
        return authnStatement;
    }
}

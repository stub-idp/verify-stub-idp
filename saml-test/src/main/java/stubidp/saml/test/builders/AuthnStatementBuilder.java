package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.extensions.extensions.EidasAuthnContext;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.time.Instant;
import java.util.Optional;

public class AuthnStatementBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private Optional<AuthnContext> authnContext = Optional.ofNullable(AuthnContextBuilder.anAuthnContext().build());
    private Optional<Instant> authnInstant = Optional.of(Instant.now());

    private AuthnStatementBuilder() {}

    public static AuthnStatementBuilder anAuthnStatement() {
        return new AuthnStatementBuilder();
    }

    public static AuthnStatementBuilder anEidasAuthnStatement() {
        return anEidasAuthnStatement(EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL);
    }

    public static AuthnStatementBuilder anEidasAuthnStatement(String level) {
        return anAuthnStatement()
            .withAuthnContext(
                AuthnContextBuilder.anAuthnContext()
                    .withAuthnContextClassRef(
                        AuthnContextClassRefBuilder.anAuthnContextClassRef()
                            .withAuthnContextClasRefValue(level).build())
                    .build()
            );
    }

    public AuthnStatement build() {
        AuthnStatement authnStatement = openSamlXmlObjectFactory.createAuthnStatement();
        authnContext.ifPresent(authnStatement::setAuthnContext);
        authnInstant.ifPresent(authnStatement::setAuthnInstant);
        return authnStatement;
    }

    public AuthnStatementBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = Optional.ofNullable(authnContext);
        return this;
    }

    public AuthnStatementBuilder withAuthnInstant(Instant authnInstant) {
        this.authnInstant = Optional.ofNullable(authnInstant);
        return this;
    }

    public AuthnStatementBuilder withId(String s) {
        throw new UnsupportedOperationException("Implement Me!");
    }
}

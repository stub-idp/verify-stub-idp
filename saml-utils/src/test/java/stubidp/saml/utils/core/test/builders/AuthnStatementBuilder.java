package stubidp.saml.utils.core.test.builders;

import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.extensions.extensions.EidasAuthnContext;
import stubidp.saml.utils.core.test.OpenSamlXmlObjectFactory;

import java.time.Instant;
import java.util.Optional;

import static stubidp.saml.utils.core.test.builders.AuthnContextBuilder.anAuthnContext;

public class AuthnStatementBuilder {

    private static OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private Optional<AuthnContext> authnContext = Optional.ofNullable(anAuthnContext().build());
    private Optional<Instant> authnInstant = Optional.of(Instant.now());

    public static AuthnStatementBuilder anAuthnStatement() {
        return new AuthnStatementBuilder();
    }

    public static AuthnStatementBuilder anEidasAuthnStatement() {
        return anEidasAuthnStatement(EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL);
    }

    public static AuthnStatementBuilder anEidasAuthnStatement(String level) {
        return anAuthnStatement()
            .withAuthnContext(
                anAuthnContext()
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

package stubidp.saml.stubidp.stub.transformers.outbound;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;

public class IdentityProviderAuthnStatementToAuthnStatementTransformerTest extends OpenSAMLRunner {

    private final IdentityProviderAuthnStatementToAuthnStatementTransformer transformer
            = new IdentityProviderAuthnStatementToAuthnStatementTransformer(new OpenSamlXmlObjectFactory());

    @Test
    public void shouldTransformAuthnStatementWithLevel1() {
        verifyLevel(AuthnContext.LEVEL_1, IdaAuthnContext.LEVEL_1_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel2() {
        verifyLevel(AuthnContext.LEVEL_2, IdaAuthnContext.LEVEL_2_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel3() {
        verifyLevel(AuthnContext.LEVEL_3, IdaAuthnContext.LEVEL_3_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel4() {
        verifyLevel(AuthnContext.LEVEL_4, IdaAuthnContext.LEVEL_4_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevelX() {
        verifyLevel(AuthnContext.LEVEL_X, IdaAuthnContext.LEVEL_X_AUTHN_CTX);
    }

    private void verifyLevel(AuthnContext authnContext, String expectedLevel) {
        final Instant startInstant = Instant.now();
        IdentityProviderAuthnStatement originalAuthnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(authnContext)
                .build();
        AuthnStatement transformedAuthnStatement = transformer.transform(originalAuthnStatement);
        assertThat(transformedAuthnStatement.getAuthnInstant()).isBetween(startInstant, Instant.now());
        assertThat(transformedAuthnStatement.getAuthnContext().getAuthnContextClassRef().getURI()).isEqualTo(expectedLevel);
    }
}

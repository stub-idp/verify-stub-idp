package stubidp.saml.stubidp.stub.transformers.outbound;

import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.stubidp.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.IdentityProviderAuthnStatement;
import stubidp.utils.common.datetime.DateTimeFreezer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.jodatime.api.Assertions.assertThat;
import static stubidp.saml.stubidp.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;

public class IdentityProviderAuthnStatementToAuthnStatementTransformerTest extends OpenSAMLRunner {

    private IdentityProviderAuthnStatementToAuthnStatementTransformer transformer;

    @BeforeEach
    public void setup() {
        DateTimeFreezer.freezeTime();
        transformer = new IdentityProviderAuthnStatementToAuthnStatementTransformer(new OpenSamlXmlObjectFactory());
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel1() throws Exception {
        verifyLevel(AuthnContext.LEVEL_1, IdaAuthnContext.LEVEL_1_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel2() throws Exception {
        verifyLevel(AuthnContext.LEVEL_2, IdaAuthnContext.LEVEL_2_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel3() throws Exception {
        verifyLevel(AuthnContext.LEVEL_3, IdaAuthnContext.LEVEL_3_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel4() throws Exception {
        verifyLevel(AuthnContext.LEVEL_4, IdaAuthnContext.LEVEL_4_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevelX() throws Exception {
        verifyLevel(AuthnContext.LEVEL_X, IdaAuthnContext.LEVEL_X_AUTHN_CTX);
    }

    private void verifyLevel(AuthnContext authnContext, String expectedLevel) {
        IdentityProviderAuthnStatement originalAuthnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(authnContext)
                .build();

        AuthnStatement transformedAuthnStatement =
                transformer.transform(originalAuthnStatement);

        assertThat(transformedAuthnStatement.getAuthnInstant()).isEqualTo(DateTime.now());
        assertThat(transformedAuthnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()).isEqualTo(expectedLevel);
    }

    @AfterEach
    public void after(){
        DateTimeFreezer.unfreezeTime();
    }
}

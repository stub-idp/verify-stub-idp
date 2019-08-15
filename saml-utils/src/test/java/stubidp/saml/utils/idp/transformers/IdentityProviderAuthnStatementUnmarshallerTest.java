package stubidp.saml.utils.idp.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.utils.core.domain.IdentityProviderAuthnStatement;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;
import stubidp.saml.utils.core.transformers.IdentityProviderAuthnStatementUnmarshaller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static stubidp.saml.utils.core.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.utils.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.utils.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.utils.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.utils.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.utils.core.test.builders.IPAddressAttributeBuilder.anIPAddress;

@ExtendWith(MockitoExtension.class)
public class IdentityProviderAuthnStatementUnmarshallerTest {

    @Mock
    private AuthnContextFactory authnContextFactory;
    public IdentityProviderAuthnStatementUnmarshaller unmarshaller;

    @BeforeEach
    public void setUp() throws Exception {
        unmarshaller = new IdentityProviderAuthnStatementUnmarshaller(
                authnContextFactory);
    }

    @Test
    public void transform_shouldTransformAuthnStatement() throws Exception {
        AuthnContextClassRef authnContextClassRef = anAuthnContextClassRef().withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_3_AUTHN_CTX).build();
        AuthnContext authnContext = anAuthnContext()
                .withAuthnContextClassRef(authnContextClassRef)
                .build();

        AuthnStatement authnStatement = anAuthnStatement()
                .withAuthnContext(authnContext)
                .build();

        Assertion assertion = anAssertion().addAuthnStatement(authnStatement).buildUnencrypted();

        unmarshaller.fromAssertion(assertion);

        verify(authnContextFactory).authnContextForLevelOfAssurance(IdaAuthnContext.LEVEL_3_AUTHN_CTX);
    }

    @Test
    public void transform_shouldTransformClientIpAddressWhenAssertionContainsAuthnStatement() throws Exception {
        String ipAddress = "1.2.3.4";
        Assertion assertion = anAssertion()
                .addAuthnStatement(anAuthnStatement().build())
                .addAttributeStatement(anAttributeStatement()
                        .addAttribute(anIPAddress().withValue(ipAddress).build())
                        .build())
                .buildUnencrypted();

        IdentityProviderAuthnStatement authnStatement = unmarshaller.fromAssertion(assertion);

        assertThat(authnStatement.getUserIpAddress().getStringValue()).isEqualTo(ipAddress);
    }
}

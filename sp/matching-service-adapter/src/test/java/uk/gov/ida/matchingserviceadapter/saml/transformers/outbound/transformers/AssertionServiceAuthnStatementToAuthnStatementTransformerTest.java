package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AssertionServiceAuthnStatementToAuthnStatementTransformerTest extends OpenSAMLRunner {

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    private MatchingServiceAuthnStatementToAuthnStatementTransformer transformer;

    @BeforeEach
    public void setup() {
        transformer = new MatchingServiceAuthnStatementToAuthnStatementTransformer(new OpenSamlXmlObjectFactory(), clock);
    }

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

    private void verifyLevel(AuthnContext requestedLevel, String expectedLevel) {
        MatchingServiceAuthnStatement matchingServiceAuthnStatement = MatchingServiceAuthnStatement.createIdaAuthnStatement(requestedLevel);

        AuthnStatement authnStatement = transformer.transform(matchingServiceAuthnStatement);

        assertThat(authnStatement.getAuthnInstant()).isEqualTo(Instant.now(clock));
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getURI()).isEqualTo(expectedLevel);
    }

}

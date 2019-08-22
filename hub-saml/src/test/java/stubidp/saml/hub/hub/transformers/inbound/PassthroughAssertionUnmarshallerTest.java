package stubidp.saml.hub.hub.transformers.inbound;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import stubidp.saml.extensions.extensions.EidasAuthnContext;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.hub.core.test.builders.IdpFraudEventIdAttributeBuilder;
import stubidp.saml.hub.test.OpenSAMLRunner;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.FraudDetectedDetails;
import stubidp.saml.utils.core.domain.PassthroughAssertion;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.saml.utils.core.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.utils.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.utils.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.utils.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.utils.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.utils.core.test.builders.Gpg45StatusAttributeBuilder.aGpg45StatusAttribute;
import static stubidp.saml.utils.core.test.builders.IPAddressAttributeBuilder.anIPAddress;

@ExtendWith(MockitoExtension.class)
public class PassthroughAssertionUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private XmlObjectToBase64EncodedStringTransformer<Assertion> assertionStringTransformer;
    @Mock
    private AuthnContextFactory authnContextFactory;

    private PassthroughAssertionUnmarshaller unmarshaller;

    @BeforeEach
    public void setup() {
        unmarshaller = new PassthroughAssertionUnmarshaller(assertionStringTransformer, authnContextFactory);
    }

    @Test
    public void shouldMapEidasLoACorrectly() {
        final AuthnContextClassRef authnContextClassRef = anAuthnContextClassRef().withAuthnContextClasRefValue(EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL).build();
        Assertion theAssertion = anAssertion()
            .addAuthnStatement(anAuthnStatement().withAuthnContext(anAuthnContext().withAuthnContextClassRef(authnContextClassRef).build()).build())
            .buildUnencrypted();
        when(authnContextFactory.mapFromEidasToLoA(EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL)).thenReturn(AuthnContext.LEVEL_2);
        when(assertionStringTransformer.apply(theAssertion)).thenReturn("AUTHN_ASSERTION");

        PassthroughAssertion authnStatementAssertion = unmarshaller.fromAssertion(theAssertion, true);
        assertThat(authnStatementAssertion.getAuthnContext().isPresent()).isEqualTo(true);
        assertThat(authnStatementAssertion.getAuthnContext().get()).isEqualTo(AuthnContext.LEVEL_2);
    }

    @Test
    public void transform_shouldHandleFraudAuthnStatementAndSetThatAssertionIsForFraudulentEventAndSetFraudDetails() throws Exception {
        final AuthnContextClassRef authnContextClassRef = anAuthnContextClassRef().withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_X_AUTHN_CTX).build();
        Assertion theAssertion = anAssertion()
                .addAuthnStatement(anAuthnStatement().withAuthnContext(anAuthnContext().withAuthnContextClassRef(authnContextClassRef).build()).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(IdpFraudEventIdAttributeBuilder.anIdpFraudEventIdAttribute().build()).addAttribute(aGpg45StatusAttribute().build()).build())
                .buildUnencrypted();
        when(authnContextFactory.authnContextForLevelOfAssurance(IdaAuthnContext.LEVEL_X_AUTHN_CTX)).thenReturn(AuthnContext.LEVEL_X);
        when(assertionStringTransformer.apply(theAssertion)).thenReturn("AUTHN_ASSERTION");

        PassthroughAssertion authnStatementAssertion = unmarshaller.fromAssertion(theAssertion);

        assertThat(authnStatementAssertion.isFraudulent()).isEqualTo(true);
        assertThat(authnStatementAssertion.getFraudDetectedDetails().isPresent()).isEqualTo(true);
    }

    @Test
    public void transform_shouldThrowExceptionWhenFraudIndicatorAuthnStatementDoesNotContainUniqueId() throws Exception {
        Assertion theAssertion = anAssertion()
                .addAuthnStatement(anAuthnStatement()
                        .withAuthnContext(anAuthnContext()
                                .withAuthnContextClassRef(
                                        anAuthnContextClassRef()
                                                .withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_X_AUTHN_CTX)
                                                .build())
                                .build())
                        .build())
                .buildUnencrypted();

        when(authnContextFactory.authnContextForLevelOfAssurance(IdaAuthnContext.LEVEL_X_AUTHN_CTX)).thenReturn(AuthnContext.LEVEL_X);

        when(assertionStringTransformer.apply(theAssertion)).thenReturn("AUTHN_ASSERTION");

        Assertions.assertThrows(IllegalStateException.class, () -> unmarshaller.fromAssertion(theAssertion));
    }

    @Test
    public void transform_shouldTransformTheIdpFraudEventIdForAFraudAssertion() throws Exception {
        String fraudEventId = "Fraud Id";
        Assertion theAssertion = anAssertion()
                .addAuthnStatement(anAuthnStatement()
                        .withAuthnContext(anAuthnContext()
                                .withAuthnContextClassRef(
                                        anAuthnContextClassRef()
                                                .withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_X_AUTHN_CTX)
                                                .build())
                                .build())
                        .build())
                .addAttributeStatement(anAttributeStatement()
                        .addAttribute(IdpFraudEventIdAttributeBuilder.anIdpFraudEventIdAttribute().withValue(fraudEventId).build())
                        .addAttribute(aGpg45StatusAttribute().build())
                        .build())
                .buildUnencrypted();

        when(authnContextFactory.authnContextForLevelOfAssurance(IdaAuthnContext.LEVEL_X_AUTHN_CTX)).thenReturn(AuthnContext.LEVEL_X);

        PassthroughAssertion passthroughAssertion = unmarshaller.fromAssertion(theAssertion);

        FraudDetectedDetails fraudDetectedDetails = passthroughAssertion.getFraudDetectedDetails().get();
        assertThat(fraudDetectedDetails.getIdpFraudEventId()).isEqualTo(fraudEventId);

    }

    @Test
    public void transform_shouldTransformTheGpg45StatusIt01ForAFraudAssertion() throws Exception {
        String gpg45Status = "IT01";
        Assertion theAssertion = givenAFraudEventAssertion(gpg45Status);

        PassthroughAssertion passthroughAssertion = unmarshaller.fromAssertion(theAssertion);

        FraudDetectedDetails fraudDetectedDetails = passthroughAssertion.getFraudDetectedDetails().get();
        assertThat(fraudDetectedDetails.getFraudIndicator()).isEqualTo(gpg45Status);
    }

    @Test
    public void transform_shouldTransformTheGpg45StatusFi01ForAFraudAssertion() throws Exception {
        String gpg45Status = "FI01";
        Assertion theAssertion = givenAFraudEventAssertion(gpg45Status);

        PassthroughAssertion passthroughAssertion = unmarshaller.fromAssertion(theAssertion);

        FraudDetectedDetails fraudDetectedDetails = passthroughAssertion.getFraudDetectedDetails().get();
        assertThat(fraudDetectedDetails.getFraudIndicator()).isEqualTo(gpg45Status);
    }

    @Test
    public void transform_shouldTransformTheGpg45StatusDF01ForAFraudAssertion() throws Exception {
        String gpg45Status = "DF01";
        Assertion theAssertion = givenAFraudEventAssertion(gpg45Status);

        PassthroughAssertion passthroughAssertion = unmarshaller.fromAssertion(theAssertion);

        FraudDetectedDetails fraudDetectedDetails = passthroughAssertion.getFraudDetectedDetails().get();
        assertThat(fraudDetectedDetails.getFraudIndicator()).isEqualTo(gpg45Status);
    }

    @Test
    public void transform_shouldThrowExceptionIfGpg45StatusIsNotRecognised() throws Exception {
        String gpg45Status = "status not known";
        Assertion theAssertion = givenAFraudEventAssertion(gpg45Status);

        Assertions.assertThrows(IllegalStateException.class, () -> unmarshaller.fromAssertion(theAssertion));
    }

    @Test
    public void transform_shouldNotSetFraudlentFlagForNotFraudulentEvent() throws Exception {
        final AuthnContextClassRef authnContextClassRef = anAuthnContextClassRef().withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_3_AUTHN_CTX).build();
        Assertion theAssertion = anAssertion()
                .addAuthnStatement(anAuthnStatement().withAuthnContext(anAuthnContext().withAuthnContextClassRef(authnContextClassRef).build()).build())
                .buildUnencrypted();

        when(authnContextFactory.authnContextForLevelOfAssurance(IdaAuthnContext.LEVEL_3_AUTHN_CTX)).thenReturn(AuthnContext.LEVEL_3);
        when(assertionStringTransformer.apply(theAssertion)).thenReturn("AUTHN_ASSERTION");

        PassthroughAssertion authnStatementAssertion = unmarshaller.fromAssertion(theAssertion);
        assertThat(authnStatementAssertion.isFraudulent()).isEqualTo(false);
        assertThat(authnStatementAssertion.getFraudDetectedDetails().isPresent()).isEqualTo(false);
    }


    @Test
    public void transform_shouldTransformIpAddress() throws Exception {
        String ipAddy = "1.2.3.4";
        Assertion theAssertion = anAssertion()
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().withValue(ipAddy).build()).build())
                .buildUnencrypted();

        PassthroughAssertion authnStatementAssertion = unmarshaller.fromAssertion(theAssertion);
        assertThat(authnStatementAssertion.getPrincipalIpAddressAsSeenByIdp().isPresent()).isEqualTo(true);
        assertThat(authnStatementAssertion.getPrincipalIpAddressAsSeenByIdp().get()).isEqualTo(ipAddy);
    }

    private Assertion givenAFraudEventAssertion(final String gpg45Status) {
        Assertion theAssertion = anAssertion()
                .addAuthnStatement(anAuthnStatement()
                        .withAuthnContext(anAuthnContext()
                                .withAuthnContextClassRef(
                                        anAuthnContextClassRef()
                                                .withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_X_AUTHN_CTX)
                                                .build()
                                )
                                .build())
                        .build())
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAttribute(
                                        IdpFraudEventIdAttributeBuilder.anIdpFraudEventIdAttribute()
                                                .withValue("my-fraud-event-id")
                                                .build())
                                .addAttribute(
                                        aGpg45StatusAttribute()
                                                .withValue(gpg45Status)
                                                .build())
                                .build())
                .buildUnencrypted();

        when(authnContextFactory.authnContextForLevelOfAssurance(IdaAuthnContext.LEVEL_X_AUTHN_CTX)).thenReturn(AuthnContext.LEVEL_X);
        return theAssertion;
    }
}

package uk.gov.ida.matchingserviceadapter.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static stubidp.saml.test.builders.ConditionsBuilder.aConditions;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.SignatureBuilder.aSignature;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.aMatchingDatasetAssertionWithSignature;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.anAuthnStatementAssertion;
import static uk.gov.ida.matchingserviceadapter.services.VerifyAssertionServiceTest.anIdpSignature;

@ExtendWith(MockitoExtension.class)
public class AttributeQueryServiceTest extends OpenSAMLRunner {

    private AttributeQueryService attributeQueryService;

    @Mock
    private AttributeQuerySignatureValidator attributeQuerySignatureValidator;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private VerifyAssertionService verifyAssertionService;

    @Mock
    private EidasAssertionService eidasAssertionService;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    private Assertion eidasAssertion;

    @BeforeEach
    public void setUp() throws Exception {
        attributeQueryService = new AttributeQueryService(
                attributeQuerySignatureValidator,
                instantValidator,
                verifyAssertionService,
                eidasAssertionService,
                userIdHashFactory,
                HUB_ENTITY_ID
        );
        eidasAssertion = anEidasAssertion("requestId", STUB_COUNTRY_ONE, anEidasSignature());
    }

    @Test
    public void shouldValidateSignatureAndIssueInstant() {
        doNothing().when(attributeQuerySignatureValidator).validate(any());
        doNothing().when(instantValidator).validate(any(), any());

        AttributeQuery attributeQuery = anAttributeQuery().build();

        attributeQueryService.validate(attributeQuery);
        verify(attributeQuerySignatureValidator, times(1)).validate(attributeQuery);
        verify(instantValidator, times(1)).validate(eq(attributeQuery.getIssueInstant()), any());
    }

    @Test
    public void shouldUseVerifyAssertionServiceWhenValidatingVerifyAssertions() {
        List<Assertion> assertions = asList(aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted(),
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted());

        attributeQueryService.validateAssertions("requestId", assertions);
        verify(verifyAssertionService, times(1)).validate("requestId", assertions);
    }


    @Test
    public void shouldUseEidasAssertionServiceWhenValidatingEidasAssertions() {
        List<Assertion> assertions = singletonList(eidasAssertion);

        attributeQueryService.validateAssertions("requestId", assertions);
        verify(eidasAssertionService, times(1)).validate("requestId", assertions);
    }

    public static Signature anEidasSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Assertion anEidasAssertion(String requestId, String issuerId, Signature assertionSignature) {
        return anAssertion()
                .withSubject(
                        aSubject().withSubjectConfirmation(
                                aSubjectConfirmation().withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withInResponseTo(requestId)
                                                .build())
                                        .build())
                                .build())
                .withIssuer(
                        anIssuer()
                                .withIssuerId(issuerId)
                                .build())
                .addAttributeStatement(anEidasAttributeStatement().build())
                .addAuthnStatement(anEidasAuthnStatement().build())
                .withSignature(assertionSignature)
                .withConditions(aConditions().build())
                .buildUnencrypted();
    }
}
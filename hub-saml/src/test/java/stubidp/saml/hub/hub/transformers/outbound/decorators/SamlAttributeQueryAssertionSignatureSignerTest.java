package stubidp.saml.hub.hub.transformers.outbound.decorators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.security.credential.Credential;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.support.PrivateKeyStoreFactory;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.common.string.StringEncoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.SimpleStringAttributeBuilder.aSimpleStringAttribute;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@ExtendWith(MockitoExtension.class)
public class SamlAttributeQueryAssertionSignatureSignerTest extends OpenSAMLRunner {

    private OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();

    @Mock
    private IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever;
    public SamlAttributeQueryAssertionSignatureSigner assertionSignatureSigner;

    @BeforeEach
    public void setUp() throws Exception {
        assertionSignatureSigner = new SamlAttributeQueryAssertionSignatureSigner(
                keyStoreCredentialRetriever,
                samlObjectFactory,
                TestEntityIds.HUB_ENTITY_ID);
    }

    @Test
    public void decorate_shouldSetSignatureOnAssertionIssuedByTheHub() {
        Credential hubSigningCredential = createHubSigningCredential();
        when(keyStoreCredentialRetriever.getSigningCredential()).thenReturn(hubSigningCredential);
        AttributeQuery inputAttributeQuery = anAttributeQueryWithHubSignature();

        AttributeQuery attributeQuery = assertionSignatureSigner.signAssertions(inputAttributeQuery);

        final Credential assertionSigningCredential = getAssertionSigningCredential(attributeQuery);
        assertThat(assertionSigningCredential).isSameAs(hubSigningCredential);
        verify(keyStoreCredentialRetriever, times(1)).getSigningCredential();
        verifyNoMoreInteractions(keyStoreCredentialRetriever);
    }

    private Credential createHubSigningCredential() {
        return new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, StringEncoding.toBase64Encoded(
                new PrivateKeyStoreFactory().create(TestEntityIds.HUB_ENTITY_ID).getSigningPrivateKey()
                        .getEncoded()
        )).getSigningCredential();
    }

    private Credential getAssertionSigningCredential(final AttributeQuery attributeQuery) {
        Assertion cycle3DatasetAssertion = (Assertion) attributeQuery.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(Assertion.TYPE_NAME).get(0);
        return cycle3DatasetAssertion.getSignature().getSigningCredential();
    }

    private AttributeQuery anAttributeQueryWithHubSignature() {
        return anAttributeQuery()
                .withSubject(
                        aSubject()
                                .withSubjectConfirmation(
                                        aSubjectConfirmation()
                                                .withSubjectConfirmationData(
                                                        aSubjectConfirmationData()
                                                                .addAssertion(
                                                                        unencyptedCycle3DatasetAssertion()
                                                                ).build()
                                                ).build()
                                ).build()
                ).build();
    }

    private Assertion unencyptedCycle3DatasetAssertion() {
        return anAssertion()
                .withIssuer(
                        hubIssuer()
                )
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAttribute(
                                        aSimpleStringAttribute()
                                                .withName(
                                                        "NINO"
                                                )
                                                .withSimpleStringValue(
                                                        "MyNINO"
                                                )
                                                .build()
                                )
                                .build()
                )
                .buildUnencrypted();
    }

    private Issuer hubIssuer() {
        return anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build();
    }
}

package stubidp.saml.hub.core.transformers.outbound.decorators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.KeyStoreBackedEncryptionCredentialResolver;
import stubidp.saml.test.OpenSAMLRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@ExtendWith(MockitoExtension.class)
public class SamlAttributeQueryAssertionEncrypterTest extends OpenSAMLRunner {

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    public Credential credential;
    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    public KeyStoreBackedEncryptionCredentialResolver credentialResolver;
    public final EntityToEncryptForLocator entityToEncryptForLocator = mock(EntityToEncryptForLocator.class);
    public final EncrypterFactory encrypterFactory = mock(EncrypterFactory.class);
    public final Encrypter encrypter = mock(Encrypter.class);

    public AttributeQuery attributeQuery;
    public EncryptedAssertion encryptedAssertion;
    public SamlAttributeQueryAssertionEncrypter samlAttributeQueryAssertionEncrypter;
    public Assertion assertion;

    @BeforeEach
    public void setUp() throws Exception {
        assertion = anAssertion().buildUnencrypted();
        attributeQuery = anAttributeQueryWithAssertion(assertion);
        encryptedAssertion = anAssertion().build();
        when(entityToEncryptForLocator.fromRequestId(anyString())).thenReturn("some id");
        when(credentialResolver.getEncryptingCredential("some id")).thenReturn(credential);
        when(encrypterFactory.createEncrypter(credential)).thenReturn(encrypter);
        when(encrypter.encrypt(assertion)).thenReturn(encryptedAssertion);
        samlAttributeQueryAssertionEncrypter = new SamlAttributeQueryAssertionEncrypter(
                credentialResolver,
                encrypterFactory,
                entityToEncryptForLocator
        );
    }

    @Test
    public void shouldConvertAssertionIntoEncryptedAssertion() throws EncryptionException {
        final AttributeQuery decoratedAttributeQuery = samlAttributeQueryAssertionEncrypter.encryptAssertions(attributeQuery);

        final SubjectConfirmationData subjectConfirmationData = decoratedAttributeQuery.getSubject()
                .getSubjectConfirmations()
                .get(0)
                .getSubjectConfirmationData();
        final List<XMLObject> encryptedAssertions = subjectConfirmationData
                .getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME);
        assertThat(encryptedAssertions.size()).isEqualTo(1);
        assertThat((EncryptedAssertion) encryptedAssertions.get(0)).isEqualTo(encryptedAssertion);

        final List<XMLObject> unencryptedAssertions = subjectConfirmationData
                .getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
        assertThat(unencryptedAssertions.size()).isEqualTo(0);
    }

    @Test
    public void decorate_shouldWrapEncryptionAssertionInSamlExceptionWhenEncryptionFails() throws EncryptionException {
        EncryptionException encryptionException = new EncryptionException("BLAM!");
        when(encrypter.encrypt(assertion)).thenThrow(encryptionException);

        SamlAttributeQueryAssertionEncrypter assertionEncrypter =
                new SamlAttributeQueryAssertionEncrypter(
                        credentialResolver,
                        encrypterFactory,
                        entityToEncryptForLocator
                );

        try {
            assertionEncrypter.encryptAssertions(attributeQuery);
        } catch (Exception e) {
            assertThat(e.getCause()).isEqualTo(encryptionException);
            return;
        }
        fail("Should never get here");
    }

    private AttributeQuery anAttributeQueryWithAssertion(final Assertion assertion) {
        return anAttributeQuery()
                    .withSubject(
                            aSubject()
                                    .withSubjectConfirmation(
                                            aSubjectConfirmation()
                                                    .withSubjectConfirmationData(
                                                            aSubjectConfirmationData()
                                                                    .addAssertion(assertion)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
    }
}

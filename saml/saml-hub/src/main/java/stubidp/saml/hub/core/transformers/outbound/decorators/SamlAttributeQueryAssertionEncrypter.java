package stubidp.saml.hub.core.transformers.outbound.decorators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.KeyStoreBackedEncryptionCredentialResolver;
import stubidp.saml.utils.core.transformers.outbound.decorators.AbstractAssertionEncrypter;

import javax.inject.Inject;
import java.util.List;

public class SamlAttributeQueryAssertionEncrypter extends AbstractAssertionEncrypter<AttributeQuery> {

    @Inject
    public SamlAttributeQueryAssertionEncrypter(
            final KeyStoreBackedEncryptionCredentialResolver credentialResolver,
            final EncrypterFactory encrypterFactory,
            final EntityToEncryptForLocator entityToEncryptForLocator) {

        super(encrypterFactory, entityToEncryptForLocator, credentialResolver);
    }

    @Override
    protected String getRequestId(final AttributeQuery attributeQuery) {
        return attributeQuery.getID();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<EncryptedAssertion> getEncryptedAssertions(final AttributeQuery attributeQuery) {
        final SubjectConfirmationData subjectConfirmationData = attributeQuery.getSubject()
                .getSubjectConfirmations()
                .get(0)
                .getSubjectConfirmationData();

        return (List<EncryptedAssertion>) (List<?>)
                subjectConfirmationData.getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Assertion> getAssertions(final AttributeQuery attributeQuery) {
        final SubjectConfirmationData subjectConfirmationData = attributeQuery.getSubject()
                .getSubjectConfirmations()
                .get(0)
                .getSubjectConfirmationData();
        return (List<Assertion>) (List<?>)
                subjectConfirmationData.getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
    }
}

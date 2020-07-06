package stubidp.saml.hub.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.domain.matching.HubEidasAttributeQueryRequest;
import stubidp.saml.domain.assertions.UserAccountCreationAttribute;
import stubidp.saml.hub.hub.factories.AttributeQueryAttributeFactory;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.assertions.HubAssertion;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer implements Function<HubEidasAttributeQueryRequest,AttributeQuery> {

    private final OpenSamlXmlObjectFactory samlObjectFactory;
    private final HubAssertionMarshaller hubAssertionMarshaller;
    private final AssertionFromIdpToAssertionTransformer assertionFromIdpTransformer;
    private final AttributeQueryAttributeFactory attributeQueryAttributeFactory;
    private final EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller;

    public HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer(
            final OpenSamlXmlObjectFactory samlObjectFactory,
            final HubAssertionMarshaller hubAssertionMarshaller,
            final AssertionFromIdpToAssertionTransformer assertionFromIdpTransformer,
            final AttributeQueryAttributeFactory attributeQueryAttributeFactory,
            final EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller) {

        this.samlObjectFactory = samlObjectFactory;
        this.hubAssertionMarshaller = hubAssertionMarshaller;
        this.assertionFromIdpTransformer = assertionFromIdpTransformer;
        this.attributeQueryAttributeFactory = attributeQueryAttributeFactory;
        this.encryptedAssertionUnmarshaller = encryptedAssertionUnmarshaller;
    }

    public AttributeQuery apply(HubEidasAttributeQueryRequest originalQuery) {
        AttributeQuery transformedQuery = samlObjectFactory.createAttributeQuery();

        Issuer issuer = samlObjectFactory.createIssuer(originalQuery.getIssuer());

        transformedQuery.setID(originalQuery.getId());
        transformedQuery.setIssuer(issuer);
        transformedQuery.setIssueInstant(Instant.now());

        if (originalQuery.getUserAccountCreationAttributes().isPresent()){
            transformedQuery.getAttributes().addAll(createAttributeList(originalQuery.getUserAccountCreationAttributes().get()));
        }

        Subject subject = samlObjectFactory.createSubject();
        NameID nameId = samlObjectFactory.createNameId(originalQuery.getPersistentId().getNameId());
        nameId.setSPNameQualifier(originalQuery.getAuthnRequestIssuerEntityId());
        nameId.setNameQualifier(originalQuery.getAssertionConsumerServiceUrl().toASCIIString());
        subject.setNameID(nameId);

        SubjectConfirmation subjectConfirmation = samlObjectFactory.createSubjectConfirmation();
        SubjectConfirmationData subjectConfirmationData = samlObjectFactory.createSubjectConfirmationData();

        final String encryptedIdentityAssertion = originalQuery.getEncryptedIdentityAssertion();
        EncryptedAssertion encryptedAssertion = encryptedAssertionUnmarshaller.transform(encryptedIdentityAssertion);
        subjectConfirmationData.getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME).add(encryptedAssertion);

        final Optional<HubAssertion> cycle3DatasetAssertion = originalQuery.getCycle3AttributeAssertion();
        if (cycle3DatasetAssertion.isPresent()) {
            Assertion transformedCycle3DatasetAssertion = hubAssertionMarshaller.toSaml(cycle3DatasetAssertion.get());
            subjectConfirmationData.getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME).add(transformedCycle3DatasetAssertion);
        }

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);

        transformedQuery.setSubject(subject);

        return transformedQuery;
    }

    private List<Attribute> createAttributeList(List<UserAccountCreationAttribute> userAccountCreationAttributes) {
        return userAccountCreationAttributes
                .stream()
                .map(attributeQueryAttributeFactory::createAttribute)
                .collect(Collectors.toList());
    }
}

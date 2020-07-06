package stubidp.saml.hub.hub.transformers.outbound;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.domain.matching.MatchingServiceHealthCheckRequest;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.function.Function;

public class MatchingServiceHealthCheckRequestToSamlAttributeQueryTransformer implements Function<MatchingServiceHealthCheckRequest,AttributeQuery> {

    private final OpenSamlXmlObjectFactory samlObjectFactory;

    @Inject
    public MatchingServiceHealthCheckRequestToSamlAttributeQueryTransformer(OpenSamlXmlObjectFactory samlObjectFactory) {
        this.samlObjectFactory = samlObjectFactory;
    }

    public AttributeQuery apply(MatchingServiceHealthCheckRequest originalQuery) {
        AttributeQuery transformedQuery = samlObjectFactory.createAttributeQuery();

        Issuer issuer = samlObjectFactory.createIssuer(originalQuery.getIssuer());

        transformedQuery.setID(originalQuery.getId());
        transformedQuery.setIssuer(issuer);
        transformedQuery.setIssueInstant(Instant.now());

        Subject subject = samlObjectFactory.createSubject();
        NameID nameId = samlObjectFactory.createNameId(originalQuery.getPersistentId().getNameId());
        nameId.setSPNameQualifier(originalQuery.getAuthnRequestIssuerEntityId());
        nameId.setNameQualifier(originalQuery.getAssertionConsumerServiceUrl().toASCIIString());
        subject.setNameID(nameId);

        SubjectConfirmation subjectConfirmation = samlObjectFactory.createSubjectConfirmation();
        SubjectConfirmationData subjectConfirmationData = samlObjectFactory.createSubjectConfirmationData();

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);

        transformedQuery.setSubject(subject);

        return transformedQuery;
    }

}

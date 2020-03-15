package stubidp.stubidp.builders;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

import javax.xml.namespace.QName;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class EidasAssertionBuilder {
    public static final String TEMPORARY_PID_TRANSLATION = "UK/EU/";

    private final Assertion assertion;

    public EidasAssertionBuilder() {
        assertion = build(Assertion.DEFAULT_ELEMENT_NAME);
    }

    public EidasAssertionBuilder withId(String id) {
        assertion.setID(id);
        return this;
    }

    public EidasAssertionBuilder withSubject(String pid, String inResponseTo, String destinationUrl) {
        assertion.setSubject(createSubject(pid, inResponseTo, destinationUrl));
        return this;
    }

    public EidasAssertionBuilder withIssuer(String issuerId) {
        assertion.setIssuer(createIssuer(issuerId));
        return this;
    }

    public EidasAssertionBuilder withIssueInstant(Instant issueInstant) {
        assertion.setIssueInstant(issueInstant);
        return this;
    }

    public EidasAssertionBuilder withConditions(String audienceUri) {
        assertion.setConditions(createConditions(audienceUri));
        return this;
    }
    public EidasAssertionBuilder addAuthnStatement(String loa, Instant authnIssueInstant) {
        assertion.getAuthnStatements().add(createAuthnStatement(loa, authnIssueInstant));
        return this;
    }

    public EidasAssertionBuilder addAttributeStatement(List<Attribute> attributes) {
        assertion.getAttributeStatements().add(createAttributeStatement(attributes));
        return this;
    }

    public Assertion build() {
        return assertion;
    }

    private Subject createSubject(String pid, String inResponseTo, String destinationUrl) {
        Subject subject = build(Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = build(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(TEMPORARY_PID_TRANSLATION + pid);
        nameID.setFormat(NameIDType.PERSISTENT);
        subject.setNameID(nameID);
        SubjectConfirmation confirmation = createSubjectConfirmation(inResponseTo, destinationUrl);
        subject.getSubjectConfirmations().add(confirmation);
        return subject;
    }

    private SubjectConfirmation createSubjectConfirmation(String inResponseTo, String destinationUrl) {
        SubjectConfirmationData subjectConfirmationData = build(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        subjectConfirmationData.setInResponseTo(inResponseTo);
        subjectConfirmationData.setNotOnOrAfter(Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(5).toInstant());
        subjectConfirmationData.setRecipient(destinationUrl);

        SubjectConfirmation subjectConfirmation = build(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        return subjectConfirmation;
    }

    private Conditions createConditions(String audienceUri) {
        Audience audience = build(Audience.DEFAULT_ELEMENT_NAME);
        audience.setURI(audienceUri);

        AudienceRestriction audienceRestriction = build(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        audienceRestriction.getAudiences().add(audience);

        Conditions conditions = build(Conditions.DEFAULT_ELEMENT_NAME);
        Instant now = Instant.now();
        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(now.atZone(ZoneId.of("UTC")).plusMinutes(5).toInstant());
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AttributeStatement createAttributeStatement(List<Attribute> attributes) {
        AttributeStatement attributeStatement = build(AttributeStatement.DEFAULT_ELEMENT_NAME);
        attributeStatement.getAttributes().addAll(attributes);
        return attributeStatement;
    }

    private AuthnStatement createAuthnStatement(String loa, Instant authnStatementAuthnInstant) {
        AuthnStatement authnStatement = build(AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = build(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setURI(loa);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(authnStatementAuthnInstant);
        return authnStatement;
    }

    private Issuer createIssuer(String issuerId) {
        Issuer issuer = build(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(issuerId);
        return issuer;
    }

    @SuppressWarnings({"unchecked", "cast"})
    private static <T extends XMLObject> T build(QName elementName) {
        return (T) XMLObjectSupport.buildXMLObject(elementName);
    }
}

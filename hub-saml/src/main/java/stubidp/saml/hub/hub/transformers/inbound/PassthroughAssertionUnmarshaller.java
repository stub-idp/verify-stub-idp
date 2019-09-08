package stubidp.saml.hub.hub.transformers.inbound;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.Gpg45Status;
import stubidp.saml.extensions.extensions.IPAddress;
import stubidp.saml.extensions.extensions.IdpFraudEventId;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.FraudDetectedDetails;
import stubidp.saml.utils.core.domain.PassthroughAssertion;
import stubidp.saml.utils.core.domain.PersistentId;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class PassthroughAssertionUnmarshaller {

    private static final List<String> VALID_GPG45_STATUSES = List.of("DF01", "FI01", "IT01");
    private final XmlObjectToBase64EncodedStringTransformer<Assertion> assertionStringTransformer;

    private final AuthnContextFactory authnContextFactory;

    public PassthroughAssertionUnmarshaller(
            XmlObjectToBase64EncodedStringTransformer<Assertion> assertionStringTransformer,
            AuthnContextFactory authnContextFactory) {

        this.assertionStringTransformer = assertionStringTransformer;
        this.authnContextFactory = authnContextFactory;
    }

    public PassthroughAssertion fromAssertion(Assertion assertion) {
        return fromAssertion(assertion, false);
    }

    public PassthroughAssertion fromAssertion(Assertion assertion, boolean isEidas) {
        PersistentId persistentId = new PersistentId(assertion.getSubject().getNameID().getValue());
        Optional<AuthnContext> levelOfAssurance = Optional.empty();
        Optional<String> principalIpAddress = getPrincipalIpAddress(assertion.getAttributeStatements());
        if (!assertion.getAuthnStatements().isEmpty()) {
            String levelOfAssuranceAsString = assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();

            levelOfAssurance = isEidas ?
                    Optional.ofNullable(authnContextFactory.mapFromEidasToLoA(levelOfAssuranceAsString)) :
                    Optional.ofNullable(authnContextFactory.authnContextForLevelOfAssurance(levelOfAssuranceAsString));
        }

        String underlyingAssertion = assertionStringTransformer.apply(assertion);

        Optional<FraudDetectedDetails> fraudDetectedDetails = Optional.empty();
        if (levelOfAssurance.isPresent() && levelOfAssurance.get().equals(AuthnContext.LEVEL_X)) {
            String idpFraudEventId = getIdpFraudEventId(assertion.getAttributeStatements());
            fraudDetectedDetails = Optional.ofNullable(new FraudDetectedDetails(idpFraudEventId, gpg45Status(assertion.getAttributeStatements())));
        }

        return new PassthroughAssertion(persistentId, levelOfAssurance, underlyingAssertion, fraudDetectedDetails, principalIpAddress);
    }

    private Optional<String> getPrincipalIpAddress(List<AttributeStatement> attributeStatements) {
        Optional<XMLObject> attribute = getAttributeNamed(attributeStatements, IdaConstants.Attributes_1_1.IPAddress.NAME);
        if (!attribute.isPresent()){
            return Optional.empty();
        }
        String ipAddress = ((IPAddress) attribute.get()).getValue();
        return Optional.ofNullable(ipAddress);
    }

    private String gpg45Status(List<AttributeStatement> attributeStatements) {
        Optional<XMLObject> gpg45StatusAttribute = getAttributeNamed(attributeStatements, IdaConstants.Attributes_1_1.GPG45Status.NAME);
        if (gpg45StatusAttribute.isPresent()) {
            String gpg45StatusValue = ((Gpg45Status) gpg45StatusAttribute.get()).getValue();
            if (VALID_GPG45_STATUSES.contains(gpg45StatusValue)) {
                return gpg45StatusValue;
            } else {
                throw new IllegalStateException(MessageFormat.format("Gpg45 status {0} is not recognised", gpg45StatusValue));
            }
        }

        throw new IllegalStateException("Fraud assertion found with no fraud indicator.");
    }

    private String getIdpFraudEventId(List<AttributeStatement> attributeStatements) {
        Optional<XMLObject> idpFraudEventAttribute = getAttributeNamed(attributeStatements, IdaConstants.Attributes_1_1.IdpFraudEventId.NAME);
        if (idpFraudEventAttribute.isPresent()) {
            return ((IdpFraudEventId) idpFraudEventAttribute.get()).getValue();
        }
        throw new IllegalStateException("Fraud assertion found with no Idp Fraud Event Id");
    }

    private Optional<XMLObject> getAttributeNamed(List<AttributeStatement> attributeStatements, String attributeName) {
        for (AttributeStatement attributeStatement : attributeStatements) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(attributeName)) {
                    return Optional.ofNullable(attribute.getAttributeValues().get(0));
                }
            }
        }
        return Optional.empty();
    }

}

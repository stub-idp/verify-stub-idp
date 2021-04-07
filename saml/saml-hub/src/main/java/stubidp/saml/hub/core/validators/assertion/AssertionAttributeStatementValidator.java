package stubidp.saml.hub.core.validators.assertion;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.IdpFraudEventId;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.util.Optional;

import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidAttributeLanguageInAssertion;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidFraudAttribute;

public class AssertionAttributeStatementValidator {

    public static final String INVALID_FRAUD_EVENT_TYPE = "Invalid fraud event type";
    private static final String INVALID_FRAUD_EVENT_NAME = "Invalid fraud event name";
    private static final String INVALID_NUMBER_OF_FRAUD_EVENT_ATTRIBUTE_STATEMENTS = "Invalid number of fraud event attribute statements";

    public void validate(Assertion assertion) {
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                for (XMLObject attributeValue : attribute.getAttributeValues()) {
                    if (attributeValue instanceof PersonName personName) {
                        String language = personName.getLanguage();
                        if (language != null && !IdaConstants.IDA_LANGUAGE.equals(language)) {
                            SamlValidationSpecificationFailure failure = invalidAttributeLanguageInAssertion(attribute.getName(), language);
                            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
                        }
                    }
                }
            }
        }
    }

    public void validateFraudEvent(Assertion assertion) {
        if(assertion.getAttributeStatements().size() != 1){
            SamlValidationSpecificationFailure failure = invalidFraudAttribute(INVALID_NUMBER_OF_FRAUD_EVENT_ATTRIBUTE_STATEMENTS);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        else {
            AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);
            validateFraudEvent(attributeStatement);
        }
    }

    private void validateFraudEvent(AttributeStatement attributeStatement) {
        Optional<Attribute> fraudEventAttribute = attributeStatement.getAttributes().stream()
                .filter(attribute -> IdaConstants.Attributes_1_1.IdpFraudEventId.NAME.equals(attribute.getName()))
                .findFirst();
        if(fraudEventAttribute.isPresent()) {
            boolean didNotDeserializeCorrectlyIntoFraudEventType = !(fraudEventAttribute.get().getAttributeValues().get(0) instanceof IdpFraudEventId);
            if (didNotDeserializeCorrectlyIntoFraudEventType) {
                SamlValidationSpecificationFailure failure = invalidFraudAttribute(INVALID_FRAUD_EVENT_TYPE);
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            }
        } else {
            SamlValidationSpecificationFailure failure = invalidFraudAttribute(INVALID_FRAUD_EVENT_NAME);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

}

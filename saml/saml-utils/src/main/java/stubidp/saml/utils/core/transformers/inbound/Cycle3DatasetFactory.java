package stubidp.saml.utils.core.transformers.inbound;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.domain.assertions.Cycle3Dataset;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static stubidp.saml.utils.hub.errors.SamlTransformationErrorFactory.missingAttributeStatementInAssertion;

public class Cycle3DatasetFactory {

    public Cycle3Dataset createCycle3DataSet(Assertion assertion) {
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();

        if (attributeStatements.size() != 1) {
            SamlValidationSpecificationFailure failure = missingAttributeStatementInAssertion(assertion.getID());
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        List<Attribute> attributes = attributeStatements.get(0).getAttributes();
        Map<String, String> data = new HashMap<>();
        for (Attribute attribute : attributes) {
            data.put(attribute.getName(), ((StringBasedMdsAttributeValue)attribute.getAttributeValues().get(0)).getValue());
        }

        return Cycle3Dataset.createFromData(data);
    }
}

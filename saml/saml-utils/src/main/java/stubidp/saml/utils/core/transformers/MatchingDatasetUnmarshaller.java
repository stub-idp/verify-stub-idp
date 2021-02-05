package stubidp.saml.utils.core.transformers;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.matching.MatchingDatasetBuilder;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class has been abstracted into a hierarchy. If you were using it as a concrete class for uk idp matching sets
 * It is suggested that you use {@link VerifyMatchingDatasetUnmarshaller} instead.
 */
public abstract class MatchingDatasetUnmarshaller {

    public MatchingDatasetUnmarshaller() {
    }

    public MatchingDataset fromAssertion(Assertion assertion) {
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (attributeStatements.isEmpty()) {
            // this returns null, and the consumer would wrap it with fromNullable.  Not awesome but it works.
            return null;
        }

        List<Attribute> attributes = attributeStatements.get(0).getAttributes();
        MatchingDatasetBuilder datasetBuilder = new MatchingDatasetBuilder();
        for (Attribute attribute : attributes) {
            transformAttribute(attribute, datasetBuilder);
        }

        datasetBuilder.personalId(getPersonalIdentifier(assertion));

        return datasetBuilder.build();
    }

    protected abstract void transformAttribute(Attribute attribute, MatchingDatasetBuilder datasetBuilder);

    protected abstract String getPersonalIdentifier(Assertion assertion);

    final List<SimpleMdsValue<LocalDate>> getBirthDates(Attribute attribute) {
        List<SimpleMdsValue<LocalDate>> birthDates = new ArrayList<>();

        for (XMLObject xmlObject : attribute.getAttributeValues()) {
            StringBasedMdsAttributeValue stringBasedMdsAttributeValue = (StringBasedMdsAttributeValue) xmlObject;
            String dateOfBirthString = stringBasedMdsAttributeValue.getValue();
            birthDates.add(new SimpleMdsValue<>(
                    LocalDate.parse(dateOfBirthString),
                    stringBasedMdsAttributeValue.getFrom(),
                    stringBasedMdsAttributeValue.getTo(),
                    stringBasedMdsAttributeValue.getVerified()));
        }

        return birthDates;
    }
}

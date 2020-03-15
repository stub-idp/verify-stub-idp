package stubidp.saml.utils.core.transformers;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.extensions.eidas.TransliterableString;
import stubidp.saml.utils.core.domain.SimpleMdsValue;
import stubidp.saml.utils.core.domain.TransliterableMdsValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class EidasMatchingDatasetUnmarshaller extends MatchingDatasetUnmarshaller{

    private static final Logger LOG = LoggerFactory.getLogger(EidasMatchingDatasetUnmarshaller.class);

    @Override
    protected void transformAttribute(Attribute attribute, MatchingDatasetBuilder datasetBuilder) {
        switch (attribute.getName()) {
            case IdaConstants.Eidas_Attributes.FirstName.NAME:
                datasetBuilder.addFirstNames(transformEidasGivenNameAttribute(attribute));
                break;

            case IdaConstants.Eidas_Attributes.FamilyName.NAME:
                datasetBuilder.addSurnames(transformEidasFamilyNameAttribute(attribute));
                break;

            case IdaConstants.Eidas_Attributes.DateOfBirth.NAME:
                datasetBuilder.dateOfBirth(transformEidasDateOfBirthAttribute(attribute));
                break;
                
            case IdaConstants.Eidas_Attributes.PersonIdentifier.NAME:
                // This is set on the datasetBuilder in the abstract base class - see getPersonalIdentifier
                break;

            /* Multiple countries still send Gender even when it is not requested so this avoids the fall-through to
            the error message */
            case IdaConstants.Eidas_Attributes.Gender.NAME:
                break;

            default:
                String errorMessage = format("Attribute {0} is not a supported Eidas Matching Dataset attribute.", attribute.getName());
                LOG.warn(errorMessage);
                throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    protected String getPersonalIdentifier(Assertion assertion) {
        return assertion.getAttributeStatements().get(0).getAttributes()
                .stream()
                .filter(a -> a.getName().equals(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME))
                .findFirst()
                .map(a -> ((PersonIdentifier)a.getAttributeValues().get(0)).getPersonIdentifier())
                .orElseThrow(() -> {
                    String errorMessage = "No PersonalIdentifier found in Matching Dataset Assertion";
                    LOG.warn(errorMessage);
                    return new IllegalArgumentException(errorMessage);
                });
    }


    private List<TransliterableMdsValue> transformEidasGivenNameAttribute(Attribute attribute) {
        Map<Boolean, List<CurrentGivenName>> attributeValueMap = attribute.getAttributeValues()
                .stream()
                .map(a -> (CurrentGivenName)a)
                .collect(Collectors.groupingBy(TransliterableString::isLatinScript));

        return singletonList(new TransliterableMdsValue(attributeValueMap.get(true).get(0).getFirstName(),
                attributeValueMap
                        .getOrDefault(false, emptyList())
                        .stream()
                        .findFirst()
                        .map(CurrentGivenName::getFirstName)
                        .orElse(null)));
    }

    private List<TransliterableMdsValue> transformEidasFamilyNameAttribute(Attribute attribute) {
        Map<Boolean, List<CurrentFamilyName>> attributeValueMap = attribute.getAttributeValues()
                .stream()
                .map(a -> (CurrentFamilyName)a)
                .collect(Collectors.groupingBy(TransliterableString::isLatinScript));

        return singletonList(new TransliterableMdsValue(attributeValueMap.get(true).get(0).getFamilyName(),
                attributeValueMap
                        .getOrDefault(false, emptyList())
                        .stream()
                        .findFirst()
                        .map(CurrentFamilyName::getFamilyName)
                        .orElse(null)));
    }

    private List<SimpleMdsValue<Instant>> transformEidasDateOfBirthAttribute(Attribute attribute) {
        List<SimpleMdsValue<Instant>> datesOfBirth = new ArrayList<>();

        for (XMLObject xmlObject : attribute.getAttributeValues()) {
            DateOfBirth dateOfBirth = (DateOfBirth) xmlObject;
            // There are no from/to dates for eIDAS attributes
            datesOfBirth.add(new SimpleMdsValue<>(dateOfBirth.getDateOfBirth(), null, null, true));
        }
        return datesOfBirth;
    }
}

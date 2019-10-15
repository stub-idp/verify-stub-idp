package stubsp.stubsp.saml.response.eidas;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.slf4j.event.Level;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlTransformationErrorManager;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.attributeStatementEmpty;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidAttributeNameFormat;

public class EidasAttributeStatementAssertionValidator {

    public EidasAttributeStatementAssertionValidator() {
    }

    private static final Set<String> VALID_EIDAS_ATTRIBUTE_NAMES = Set.of(
            Eidas_Attributes.FirstName.NAME,
            Eidas_Attributes.FamilyName.NAME,
            Eidas_Attributes.DateOfBirth.NAME,
            Eidas_Attributes.PersonIdentifier.NAME,
            Eidas_Attributes.CurrentAddress.NAME,
            Eidas_Attributes.Gender.NAME
    );

    private static final Set<String> VALID_ATTRIBUTE_NAME_FORMATS = Set.of(
            Attribute.URI_REFERENCE
    );

    private static final Map<String, QName> VALID_TYPE_FOR_ATTRIBUTE = Map.<String, QName>ofEntries(
            Map.entry(Eidas_Attributes.FirstName.NAME, CurrentGivenName.TYPE_NAME),
            Map.entry(Eidas_Attributes.FamilyName.NAME, CurrentFamilyName.TYPE_NAME),
            Map.entry(Eidas_Attributes.DateOfBirth.NAME, DateOfBirth.TYPE_NAME),
            Map.entry(Eidas_Attributes.PersonIdentifier.NAME, PersonIdentifier.TYPE_NAME),
            Map.entry(Eidas_Attributes.CurrentAddress.NAME, CurrentAddress.TYPE_NAME),
            Map.entry(Eidas_Attributes.Gender.NAME, EidasGender.TYPE_NAME)
    );

    private static final Map<String, String> MANDATORY_ATTRIBUTES = Map.of(
            Eidas_Attributes.FirstName.NAME, Eidas_Attributes.FirstName.FRIENDLY_NAME,
            Eidas_Attributes.FamilyName.NAME, Eidas_Attributes.FamilyName.FRIENDLY_NAME,
            Eidas_Attributes.DateOfBirth.NAME, Eidas_Attributes.DateOfBirth.FRIENDLY_NAME,
            Eidas_Attributes.PersonIdentifier.NAME, Eidas_Attributes.PersonIdentifier.FRIENDLY_NAME);

    public void validate(Assertion assertion) {
        validateAttributes(assertion);
    }

    private void validateAttributes(Assertion assertion) {
        final List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (attributeStatements.isEmpty()) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.mdsStatementMissing();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        if (attributeStatements.size() > 1) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.mdsMultipleStatements();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        final List<Attribute> attributes = attributeStatements.get(0).getAttributes();
        if (attributes.isEmpty()) {
            SamlValidationSpecificationFailure failure = attributeStatementEmpty(assertion.getID());
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        Set<String> attributeNames = attributes.stream().map(Attribute::getName).collect(Collectors.toSet());
        if (!attributeNames.containsAll(MANDATORY_ATTRIBUTES.keySet())) {
            throw new SamlTransformationErrorException(String.format("Mandatory attributes not provided. Expected %s but got %s",
                    MANDATORY_ATTRIBUTES.values().stream().collect(Collectors.joining(",")),
                    attributes.stream().map(Attribute::getFriendlyName).collect(Collectors.joining(","))), Level.ERROR);
        }

        for (Attribute attribute : attributes) {
            final String attributeName = attribute.getName();
            if (!VALID_EIDAS_ATTRIBUTE_NAMES.contains(attributeName)) {
                SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.mdsAttributeNotRecognised(attributeName);
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            }
            if (attribute.getAttributeValues().isEmpty()) {
                SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.emptyAttribute(attributeName);
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            }
            if (!VALID_TYPE_FOR_ATTRIBUTE.get(attributeName).equals(attribute.getAttributeValues().get(0).getSchemaType())) {
                final QName schemaType = attribute.getAttributeValues().get(0).getSchemaType();
                SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.attributeWithIncorrectType(attributeName, VALID_TYPE_FOR_ATTRIBUTE.get(attributeName), schemaType);
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            }
            if (!VALID_ATTRIBUTE_NAME_FORMATS.contains(attribute.getNameFormat())) {
                SamlTransformationErrorManager.warn(
                        invalidAttributeNameFormat(attribute.getNameFormat()));
            }
        }
    }
}

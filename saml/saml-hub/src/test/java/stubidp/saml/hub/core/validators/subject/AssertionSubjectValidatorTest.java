package stubidp.saml.hub.core.validators.subject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.extensions.validation.errors.ResponseProcessingValidationSpecification;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.test.builders.NameIdBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;

public class AssertionSubjectValidatorTest extends OpenSAMLRunner {

    private static final String ASSERTION_ID = "some-assertion-id";

    private static AssertionSubjectValidator validator;

    @BeforeEach
    void setup() {
        validator = new AssertionSubjectValidator();
    }

    @Test
    void validate_shouldThrowExceptionIfSubjectElementIsMissing() {
        assertExceptionMessage(null, ResponseProcessingValidationSpecification.class, SamlTransformationErrorFactory.missingAssertionSubject(ASSERTION_ID));
    }

    @Test
    void validate_shouldThrowExceptionIfSubjectNameIdIsMissing() {
        final Subject subject = aSubject().withNameId(null).build();
        assertExceptionMessage(subject, ResponseProcessingValidationSpecification.class, SamlTransformationErrorFactory.assertionSubjectHasNoNameID(ASSERTION_ID));
    }

    @Test
    void validate_shouldThrowExceptionIfSubjectNameIdFormatAttributeIsMissing() {
        final Subject subject = aSubject().withNameId(NameIdBuilder.aNameId().withFormat(null).build()).build();
        assertExceptionMessage(subject, ResponseProcessingValidationSpecification.class, SamlTransformationErrorFactory.missingAssertionSubjectNameIDFormat(ASSERTION_ID));
    }

    @Test
    void validate_shouldSuccessfullyValidateMultipleNameIdFormats() {
        Subject subject = aSubject().withNameId(NameIdBuilder.aNameId().withFormat(NameIDType.PERSISTENT).build()).build();
        assertThat(subject.getNameID().getFormat()).isEqualTo(NameIDType.PERSISTENT);

        subject = aSubject().withNameId(NameIdBuilder.aNameId().withFormat(NameIDType.TRANSIENT).build()).build();
        assertThat(subject.getNameID().getFormat()).isEqualTo(NameIDType.TRANSIENT);
    }

    @Test
    void validate_shouldThrowExceptionIfSubjectNameIdFormatAttributeHasInvalidValue() {
        final Subject subject = aSubject().withNameId(NameIdBuilder.aNameId().withFormat("Invalid").build()).build();
        assertExceptionMessage(subject, ResponseProcessingValidationSpecification.class, SamlTransformationErrorFactory.illegalAssertionSubjectNameIDFormat(ASSERTION_ID, subject.getNameID().getFormat()));
    }

    static void assertExceptionMessage(
            final Subject subject,
            Class<? extends SamlValidationSpecificationFailure> errorClass,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(() -> validator.validate(subject, ASSERTION_ID), failure);
    }
}
